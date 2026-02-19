-- 1. Receive parameters: key=request identifier, timeout=expiration time
local key = KEYS[1]
local timeout = ARGV[1]

-- 2. Check lock: if exists return 1 (duplicate submission), if not exist set lock and return 0
if redis.call('EXISTS', key) == 1 then
    return 1
else
    redis.call('SET', key, '1', 'EX', timeout)
    return 0
end
