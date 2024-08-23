package run.game.dao.backstage.impl

import jandcode.commons.test.*
import org.junit.jupiter.api.*

class Position_Test extends Base_Test {

    @Test
    void findItems1() {
        TextPosition p0 = new TextPosition()
        TextPosition p1 = new TextPosition("sdsd")
        TextPosition p2 = new TextPosition("KdnRdfa", new Position(1, 2, 30, 20), new PagePosition(1, 3, 5))

        println(p0)
        println(p1)
        println(p2)
    }

}
