package unicon.Achiva;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class RepairFlywayTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void repair() {
        jdbcTemplate.execute("DELETE FROM flyway_schema_history WHERE version = '8'");
        System.out.println("REPAIRED FLYWAY");
    }
}
