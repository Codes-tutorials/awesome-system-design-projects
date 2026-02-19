-- rate_limiter.lua
-- Token Bucket Algorithm
-- KEYS[1]: The rate limit key (e.g., "rate_limit:user:123")
-- ARGV[1]: Refill rate (tokens per second)
-- ARGV[2]: Bucket capacity (max burst)
-- ARGV[3]: Requested tokens (cost of request, usually 1)
-- ARGV[4]: Current timestamp in seconds (or milliseconds if higher precision needed)

local key = KEYS[1]
local rate = tonumber(ARGV[1])
local capacity = tonumber(ARGV[2])
local requested = tonumber(ARGV[3])
local now = tonumber(ARGV[4])

-- Get current bucket state
-- 'tokens': current number of tokens
-- 'last_refill': timestamp of last refill
local info = redis.call("HMGET", key, "tokens", "last_refill")
local tokens = tonumber(info[1])
local last_refill = tonumber(info[2])

-- Initialize if missing
if tokens == nil then
    tokens = capacity
    last_refill = now
end

-- Refill tokens based on time passed
local delta = math.max(0, now - last_refill)
local filled_tokens = math.min(capacity, tokens + (delta * rate))

-- Check if we have enough tokens
local allowed = false
if filled_tokens >= requested then
    allowed = true
    filled_tokens = filled_tokens - requested
end

-- Update bucket state
-- Set expiry to avoid stale keys cluttering memory (e.g., 2 * capacity / rate gives plenty of buffer)
redis.call("HMSET", key, "tokens", filled_tokens, "last_refill", now)
redis.call("EXPIRE", key, math.ceil(capacity / rate * 2)) 

return allowed
