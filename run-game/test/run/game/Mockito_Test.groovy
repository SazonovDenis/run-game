package run.game

import org.junit.jupiter.api.*
import run.game.dao.RgmBase_Test

//@RunWith(PowerMockRunner.class)
//@PrepareForTest(TestClass1.class)
class Mockito_Test extends RgmBase_Test {

    @Test
    void t_1() {
        long v1 = MockitoTestClass1.methodStatic1()
        println("v1: " + v1)

        //
        println()

        //
        MockitoTestClass1 sm = new MockitoTestClass1()
        long v2 = sm.method1()
        println("v2: " + v2)
    }

    @Test
    void t_methodStatic1() {
        long v1 = MockitoTestClass1.methodStatic1()
        println("v1: " + v1)

        //
        println()

        // Мокаем статический метод
        PowerMockito.mockStatic(MockitoTestClass1.class)

        // Устанавливаем поведение мока
        Long vMocked = 100999L
        PowerMockito.when(MockitoTestClass1.methodStatic1()).thenReturn(vMocked)

        // Вызываем мокнутый метод и проверяем результат
        long v2 = MockitoTestClass1.methodStatic1()
        println("v2: " + v2)
    }

    @Test
    void t_method1() {
        MockitoTestClass1 sm = new MockitoTestClass1()
        long v1 = sm.method1()
        println("v1: " + v1)

        //
        println()

        // Мокаем метод
        MockitoTestClass1 sm2 = PowerMockito.mock(MockitoTestClass1.class)

        // Устанавливаем поведение мока
        Long vMocked = 200888L
        PowerMockito.when(sm2.method1()).thenReturn(vMocked)

        // Вызываем мокнутый метод и проверяем результат
        long v2 = sm2.method1()
        println("v2: " + v2)
    }

}
