package by.magofrays.configuration


import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import java.io.Serializable
import java.util.Objects
import java.util.UUID

@Component
class FamilyPermissionEvaluator(
    val reactiveRedisTemplate: ReactiveRedisTemplate<String, List<String>>
) : PermissionEvaluator {
    override fun hasPermission(
        authentication: Authentication,
        targetDomainObject: Any,
        permission: Any
    ): Boolean {
        return false
    }

    override fun hasPermission(
        authentication: Authentication,
        targetId: Serializable,
        targetType: String,
        permission: Any
    ): Boolean {
        if (!authentication.isAuthenticated) {
            return false
        }
        if (targetType == "family") {
            val jwt = authentication.principal as? Jwt ?: return false

            val memberId = UUID.fromString(jwt.subject)
            val familyId = targetId as UUID
            val key = "family:accesses:$familyId:$memberId"
            val accesses = getAccessesFromCache(key) ?: return false

            return accesses.any { it == permission.toString() }
        }

        return false
    }
    private fun getAccessesFromCache(key: String): List<String>? {
        return try {
            reactiveRedisTemplate.opsForValue().get(key).block()
        } catch (e: Exception) {
            null
        }
    }
}