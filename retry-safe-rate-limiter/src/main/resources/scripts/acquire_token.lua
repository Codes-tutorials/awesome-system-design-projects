-- acquire_token.lua
-- KEYS[1]: rate_limit:{user_id}
-- ARGV[1]: capacity
-- ARGV[2]: refill_rate (tokens/sec)
-- ARGV[3]: now (epoch seconds)
-- ARGV[4]: requested_tokens (default 1)

local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])

local info = redis.call("HMGET", key, "tokens", "last_refill")
local tokens = tonumber(info[1])
local last_refill = tonumber(info[2])

if not tokens then
    tokens = capacity
    last_refill = now
end

-- Refill
local elapsed = math.max(0, now - last_refill)
local refill = elapsed * rate
tokens = math.min(capacity, tokens + refill)
last_refill = now

local allowed = 0
local retry_after = 0

if tokens >= requested then
    allowed = 1
    tokens = tokens - requested
else
    allowed = 0
    local needed = requested - tokens
    retry_after = needed / rate
end

redis.call("HMSET", key, "tokens", tokens, "last_refill", last_refill)
redis.call("EXPIRE", key, 60) -- expire if idle

return {allowed, tokens, retry_after}
