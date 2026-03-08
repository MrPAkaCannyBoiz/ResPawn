package com.respawn;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Disabled because we are running unit tests without a real database connection")
class ResPawnApplicationTests
{

    @Test
    void contextLoads()
    {
    }

}
