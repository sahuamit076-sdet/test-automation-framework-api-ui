package in.zeta.qa.email;


import org.testng.annotations.Test;

@Test(singleThreaded = false)
public class TestClass3 {
    @Test
    void  class3Test1() {
        System.out.println("class3Test1 Thread : " + Thread.currentThread().getName());
    }

    @Test
    void  class3Test2() {
        System.out.println("class3Test2 Thread : " + Thread.currentThread().getName());
    }

    @Test
    void  class3Test3() {
        System.out.println("class3Test3 Thread : " + Thread.currentThread().getName());
    }
}
