import mu.KotlinLogging
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("User Database Tests.")
class UserDatabaseTests {
    @Test
    fun firstTest() {
        logger.info { "First test ran." }
    }
    companion object {
        private val logger = KotlinLogging.logger {  }
        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            logger.info { "BeforeAll was called." }
        }
        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            logger.info { "AfterAll was called." }
        }
    }
}