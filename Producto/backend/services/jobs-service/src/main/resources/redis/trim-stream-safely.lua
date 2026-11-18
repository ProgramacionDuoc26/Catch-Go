-- El limite es un objetivo: nunca recortar si no se conoce una posicion segura.
local stream = KEYS[1]
local limit = tonumber(ARGV[1])
local size = redis.call('XLEN', stream)
if not limit or limit < 1 or size <= limit then return 0 end

local groups = redis.call('XINFO', 'GROUPS', stream)
if #groups == 0 then return 0 end

-- Comparar cada parte del ID como texto decimal evita perdida de precision en Lua.
local function decimalLess(a, b)
    a = string.gsub(a, '^0+', '')
    b = string.gsub(b, '^0+', '')
    if a == '' then a = '0' end
    if b == '' then b = '0' end
    if #a ~= #b then return #a < #b end
    return a < b
end

local function idLess(a, b)
    local am, as = string.match(a, '^(%d+)%-(%d+)$')
    local bm, bs = string.match(b, '^(%d+)%-(%d+)$')
    if not am or not bm then error('ID de stream invalido') end
    if am == bm then return decimalLess(as, bs) end
    return decimalLess(am, bm)
end

-- Limitar el trabajo de cada publicacion, incluso con una acumulacion grande.
local toRemove = math.min(size - limit, 1000)
local entries = redis.call('XRANGE', stream, '-', '+', 'COUNT', toRemove + 1)
if #entries <= toRemove then return 0 end
local boundary = entries[#entries][1]

for _, group in ipairs(groups) do
    local name = nil
    local delivered = nil
    for i = 1, #group, 2 do
        if group[i] == 'name' then name = group[i + 1] end
        if group[i] == 'last-delivered-id' then delivered = group[i + 1] end
    end
    if not name or not delivered then error('Informacion incompleta del grupo') end
    if idLess(delivered, boundary) then boundary = delivered end
    local pending = redis.call('XPENDING', stream, name)
    if pending[1] > 0 then
        if not pending[2] then error('Informacion incompleta de pendientes') end
        if idLess(pending[2], boundary) then boundary = pending[2] end
    end
end

-- MINID elimina solo IDs anteriores al limite y conserva ese limite.
return redis.call('XTRIM', stream, 'MINID', boundary)
