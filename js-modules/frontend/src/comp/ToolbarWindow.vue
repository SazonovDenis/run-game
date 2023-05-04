<template>

    <jc-btn @click="doStart()" v-if="started==false">Start</jc-btn>


    <div class="container" v-if="started==true">

        <div class="header" @click="openFullscreen()">
            header
        </div>

        <div class="header-shadow">
            shadow header
        </div>

        <div class="content">
            content ============
            <br>
            Можно разрабатывать на Java, а можно разрабатывать Java. Есть люди, чей код
            исполняет
            виртуальная машина — а есть люди, чей код и есть виртуальная машина.
            <br>
            Вроде бы те и другие существуют в одной Java-экосистеме, но задачи совершенно
            разные.
            Поэтому редкое место, где они пересекаются и могут что-то поведать друг другу
            —
            Java-конференции. Мы проводим их регулярно (уже в апреле будет JPoint). И на
            предыдущей нашей конференции Иван Углянский dbg_nsk поделился с
            Java-разработчиками
            тем, как всё выглядит с его стороны.
            <br>
            Чем он вообще занимается? Почему JVM-инженеры всё так медленно делают? На
            каком
            языке
            стоит писать рантайм, а на каком компилятор? Как «папка бога» в Windows
            привела к
            неожиданным последствиям? Может ли «обычный джавист» стать JVM-инженером?

            <br>
            Поскольку все эти вопросы из доклада звучат интересно, мы решили для Хабра
            сделать
            его
            текстовую версию (а для тех, кому удобнее видео, прикладываем ссылку на ютуб).
            Далее
            повествование идёт от лица Ивана.
            <br>
            Экосистема Java состоит из двух слоев. Верхний слой — прекрасный город с
            великолепной
            архитектурой и красивыми высокими зданиями. В этом городе живут эльфы, которые
            программируют на джаве. У них есть классные фреймворки, Spring, Hibernate,
            системы
            сборки и так далее. Все высокоуровнево. А под этим красивым Ривенделом есть
            пещера
            с
            гномами. Они сидят в своей пещере, света солнечного не видят, копошатся в
            кишках и
            делают виртуальную машину. У них совсем другие темы для обсуждения: JIT,
            сборщики
            мусора, они больше говорят про C++, чем про джаву, и копаются в байтиках.
            <br>
            Я изначально как раз из этого нижнего мира — тот самый гном, который сидит и
            делает
            виртуальные машины. Восемь лет я делал это в компании Excelsior, там была
            собственная
            виртуальная машина Excelsior JET. Последние три года я занимаюсь этим в
            Huawei.
            <br>
            Так что сегодня я расскажу вам, кто такие эти гномы, что они делают и зачем
            они
            нужны.
            Расскажу, как конкретно разрабатываются фичи в JVM в бытовом смысле: как
            выглядит
            день
            JVM-инженера, как он работает? Ну и, конечно же, потравлю байки: за 11 лет я
            видел
            много чего интересного в странно работающих Java-приложениях. А еще отвечу на
            вопрос
            «Почему вы всё так долго делаете».
            <br>
            Есть разные подходы, как можно получить такой код. Можно проинтерпретировать.
            К сожалению, в некоторых кругах за джавой до сих пор закрепилось поверье, что
            это именно интерпретируемый язык, со всеми вытекающими из этого
            последствиями..
            <br>
            Конечно, в современном мире это не так. Кроме интерпретации у нас есть,
            например, JIT-компиляция, когда вы на лету компилируете что-то в машинный код,
            а уже потом он будет исполняться. Также сейчас уже довольно популярна
            AOT-компиляция джавы, когда вы заранее превращаете весь байткод в машинный
            код, как будто у вас просто приложение на C++. Раньше по-серьезному таким
            занимались только мы в Excelsior, но сейчас благодаря GraalVM и Native Image
            это стало популярнее, и никого этим уже не удивишь.
            <br>
            Кроме компиляторов, есть рантайм. Он должен поддерживать исполнение вашего
            кода, сгенерированного каким-то образом. Это очень важная часть, и сегодня
            здесь на конференции много говорил Владимир Воскресенский. О ней тоже еще
            поговорим.
            <br>
            Теперь конкретнее. Чем в этих частях занимаются JVM-инженеры? Если вы
            JVM-инженер в компиляторе, то глобально вам нужно породить тот самый машинный
            код под нужную платформу (хотите на Intel — значит, на Intel, если на ARM — то
            на ARM). При этом код должен быть корректным, должен сохранять семантику
            джава-программы. Ну и совсем уже желательно, чтобы он был эффективным, чтобы
            ваша джава-программа работала быстро после этой самой компиляции.
            <br>
            content ============
        </div>

        <div class="footer">
            footer
            <div>
                footer 1111111
            </div>
            <div>
                footer 2222
            </div>
        </div>


        <div class="footer-shadow">
            &nbsp;
        </div>

    </div>

</template>


<script>

import {jcBase} from '../vendor'

export default {

    name: "ToolbarWindow",

    components: {jcBase},

    data() {
        return {
            started: false
        }
    },

    methods: {

        doStart() {
            this.started = true
            this.openFullscreen()
        },

        /* View in fullscreen */
        openFullscreen() {
            /* Get the documentElement (<html>) to display the page in fullscreen */
            var elem = document.documentElement;

            if (elem.requestFullscreen) {
                elem.requestFullscreen();
            } else if (elem.webkitRequestFullscreen) { /* Safari */
                elem.webkitRequestFullscreen();
            } else if (elem.msRequestFullscreen) { /* IE11 */
                elem.msRequestFullscreen();
            }
        },

        /* Close fullscreen */
        closeFullscreen() {
            if (document.exitFullscreen) {
                document.exitFullscreen();
            } else if (document.webkitExitFullscreen) { /* Safari */
                document.webkitExitFullscreen();
            } else if (document.msExitFullscreen) { /* IE11 */
                document.msExitFullscreen();
            }
        },

    },

    mounted() {
        console.info("mounted")
        //.openFullscreen()
    }

}

</script>


<style>

body {
    margin: 0;
}

:root {
    --header-height: 2em;
    --footer-height: 4em;
    --header-background-color: #f0f0f0e0;
    --test-border_: 2px dashed;
    --test-border: none;
}

.header {
    border: var(--test-border);
    border-color: rgba(255, 0, 0, 0.23);
    color: rgb(192, 13, 13);
    background-color: var(--header-background-color);
    height: var(--header-height);
    z-index: 100;
}

.header-shadow {
    height: var(--header-height);
}

.content {
    border: var(--test-border);
    border-color: rgba(18, 179, 5, 0.3);
    color: rgb(14, 170, 1);
    min-height: 2em;
}

.footer {
    border: var(--test-border);
    border-color: rgba(0, 62, 255, 0.36);
    color: rgb(7, 47, 170);
    background-color: var(--header-background-color);
    height: var(--footer-height);
    z-index: 100;
}

.footer-shadow {
    height: var(--footer-height);
}

/****************************/

.header {
    position: fixed;
    top: 0;
    width: 100%;
}

.footer {
    position: fixed;
    bottom: 0;
    width: 100%;
}


</style>