-- El retroceso solo se permite sin pendientes ni consumidores registrados.
local stream = KEYS[1]
local name = ARGV[1]
local groups = redis.call('XINFO', 'GROUPS', stream)
for _, fields in ipairs(groups) do
    local info = {}
    for i = 1, #fields, 2 do info[fields[i]] = fields[i + 1] end
    if info['name'] == name then
        if info['pending'] > 0 then return -1 end
        if info['consumers'] > 0 then return -2 end
        redis.call('XGROUP', 'SETID', stream, name, '0-0')
        return 1
    end
end
return 0
