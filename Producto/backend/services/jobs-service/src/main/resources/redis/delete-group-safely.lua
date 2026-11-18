-- Comprobar y borrar en una sola operacion evita carreras con nuevas entregas.
local stream = KEYS[1]
local groupName = ARGV[1]
local groups = redis.call('XINFO', 'GROUPS', stream)
local group = nil
for _, fields in ipairs(groups) do
    local info = {}
    for i = 1, #fields, 2 do info[fields[i]] = fields[i + 1] end
    if info['name'] == groupName then group = info; break end
end
if not group then return 0 end
if group['pending'] > 0 then return -1 end
local latest = redis.call('XREVRANGE', stream, '+', '-', 'COUNT', 1)
if #latest > 0 and group['last-delivered-id'] ~= latest[1][1] then return -2 end
if group['consumers'] > 0 then return -3 end
return redis.call('XGROUP', 'DESTROY', stream, groupName)
