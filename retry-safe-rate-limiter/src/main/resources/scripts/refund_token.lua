-- refund_token.lua
-- KEYS[1]: rate_limit:{user_id}
-- ARGV[1]: capacity
-- ARGV[2]: tokens_to_refund (usually 1)

local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refund_amount = tonumber(ARGV[2])

local info = redis.call("HMGET", key, "tokens")
local tokens = tonumber(info[1])

if tokens then
    -- Only refund if the bucket exists (don't create a bucket just to refund)
    tokens = math.min(capacity, tokens + refund_amount)
    redis.call("HSET", key, "tokens", tokens)
    return tokens
else
    return -1 -- Bucket expired or doesn't exist, ignore
end
