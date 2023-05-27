package whu.edu.cs.transitnet;

import org.junit.Test;

//@RunWith(SpringRunner.class)
//@SpringBootTest
//@MapperScan("whu.edu.cs.transitnet.*")
public class randomTest {

    @Test
    public void hashcodeTest() {
        String a = "JG_B3-Weekday-SDon-130500_B6769_935";
        System.out.println(a.hashCode());
        int b =  a.hashCode();
        System.out.println(new String(String.valueOf(b)));
    }

}

