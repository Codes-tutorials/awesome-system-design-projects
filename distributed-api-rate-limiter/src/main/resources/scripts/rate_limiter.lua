-- rate_limiter.lua
-- KEYS[1]: rate_limit:{user_id}
-- ARGV[1]: capacity (e.g., 100)
-- ARGV[2]: refill_rate (tokens per second)
-- ARGV[3]: current_timestamp (in seconds)
-- ARGV[4]: requested_tokens (usually 1)

local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])

-- Get current state
-- field 1: tokens_left
-- field 2: last_refill_timestamp
local state = redis.call("HMGET", key, "tokens", "last_refill")
local tokens = tonumber(state[1])
local last_refill = tonumber(state[2])

-- Initialize if missing
if not tokens then
    tokens = capacity
    last_refill = now
end

-- Calculate refill
local delta = math.max(0, now - last_refill)
local refill = delta * refill_rate
tokens = math.min(capacity, tokens + refill)

-- Try to consume
local allowed = 0
local remaining = tokens
local retry_after = 0

if tokens >= requested then
    allowed = 1
    tokens = tokens - requested
    remaining = tokens
    -- Update Redis
    redis.call("HMSET", key, "tokens", tokens, "last_refill", now)
    -- Expire key after enough time to refill to full (cleanup)
    -- Time to full = capacity / refill_rate
    local ttl = math.ceil(capacity / refill_rate) + 1
    redis.call("EXPIRE", key, ttl)
else
    allowed = 0
    -- Calculate time to wait for enough tokens
    -- needed = requested - tokens
    -- time = needed / refill_rate
    local needed = requested - tokens
    retry_after = needed / refill_rate
end

return {allowed, remaining, retry_after}
