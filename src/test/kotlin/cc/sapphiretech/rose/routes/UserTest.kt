package cc.sapphiretech.rose.routes

import cc.sapphiretech.rose.ext.orThrow
import cc.sapphiretech.rose.models.RosePermission
import cc.sapphiretech.rose.roseTest
import io.ktor.http.*
import org.junit.Test
import kotlin.test.assertEquals

class UserTest {
    @Test
    fun validAddRole() = roseTest {
        val admin = registerSuperuser()
        val target = registerRandomUser()
        val role = createRandomRole()

        val res = authPut(
            admin.id,
            "/user/role",
            requiredPermissions = arrayOf(RosePermission.MANAGE_USERS),
            body = UserRolePut(target.id, role.id)
        )
        assertEquals(HttpStatusCode.OK, res.status)
        assert(userService.findById(target.id).orThrow { RuntimeException() }.roles.any { it.id == role.id })
    }

    @Test
    fun invalidAddRole_Forbidden() = roseTest {
        val admin = registerRandomUser(arrayOf(RosePermission.MANAGE_USERS))
        val target = registerSuperuser()
        val role = createRandomRole()

        val res = authPut(
            admin.id,
            "/user/role",
            requiredPermissions = arrayOf(RosePermission.MANAGE_USERS),
            skipAuthCheck = true,
            body = UserRolePut(target.id, role.id)
        )
        assertEquals(HttpStatusCode.Forbidden, res.status)
        assert(!userService.findById(target.id).orThrow { RuntimeException() }.roles.any { it.id == role.id })
    }

    @Test
    fun invalidAddRole_BadTargetUser() = roseTest {
        val admin = registerSuperuser()
        val role = createRandomRole()
        val res = authPut(
            admin.id,
            "/user/role",
            requiredPermissions = arrayOf(RosePermission.MANAGE_USERS),
            body = UserRolePut(Int.MAX_VALUE, role.id)
        )
        assertEquals(HttpStatusCode.NotFound, res.status)
    }

    @Test
    fun invalidAddRole_BadRole() = roseTest {
        val admin = registerSuperuser()
        val target = registerRandomUser()
        val res = authPut(
            admin.id,
            "/user/role",
            requiredPermissions = arrayOf(RosePermission.MANAGE_USERS),
            body = UserRolePut(target.id, Int.MAX_VALUE)
        )
        assertEquals(HttpStatusCode.NotFound, res.status)
    }

    @Test
    fun validRemoveRole() = roseTest {
        val admin = registerSuperuser()
        val target = registerRandomUser()
        val role = createRandomRole()
        userService.addRole(target, role)
        assert(userService.findById(target.id).orThrow { RuntimeException() }.roles.any { it.id == role.id })

        val res = authDelete(
            admin.id,
            "/user/role",
            requiredPermissions = arrayOf(RosePermission.MANAGE_USERS),
            body = UserRoleDelete(target.id, role.id)
        )
        assertEquals(HttpStatusCode.OK, res.status)
        assert(!userService.findById(target.id).orThrow { RuntimeException() }.roles.any { it.id == role.id })
    }

    @Test
    fun invalidRemoveRole_BadTargetUser() = roseTest {
        val admin = registerSuperuser()
        val role = createRandomRole()
        val res = authDelete(
            admin.id,
            "/user/role",
            requiredPermissions = arrayOf(RosePermission.MANAGE_USERS),
            body = UserRoleDelete(Int.MAX_VALUE, role.id)
        )
        assertEquals(HttpStatusCode.NotFound, res.status)
    }

    @Test
    fun invalidRemoveRole_BadRole() = roseTest {
        val admin = registerSuperuser()
        val target = registerRandomUser()
        val res = authDelete(
            admin.id,
            "/user/role",
            requiredPermissions = arrayOf(RosePermission.MANAGE_USERS),
            body = UserRoleDelete(target.id, Int.MAX_VALUE)
        )
        assertEquals(HttpStatusCode.NotFound, res.status)
    }

    @Test
    fun invalidRemoveRole_Forbidden() = roseTest {
        val admin = registerRandomUser(arrayOf(RosePermission.MANAGE_USERS))
        val target = registerSuperuser()
        val role = createRandomRole()
        userService.addRole(target, role)
        assert(userService.findById(target.id).orThrow { RuntimeException() }.roles.any { it.id == role.id })

        val res = authDelete(
            admin.id,
            "/user/role",
            requiredPermissions = arrayOf(RosePermission.MANAGE_USERS),
            skipAuthCheck = true,
            body = UserRoleDelete(target.id, role.id)
        )
        assertEquals(HttpStatusCode.Forbidden, res.status)
        assert(userService.findById(target.id).orThrow { RuntimeException() }.roles.any { it.id == role.id })
    }
}