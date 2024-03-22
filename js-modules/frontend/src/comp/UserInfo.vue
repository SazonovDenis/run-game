<template>
    <div>
        <q-input v-model="user.text"
                 label="Имя*"
                 outlined
                 stack-label :dense="true">
            <template #prepend>
                <q-icon
                    name="user"
                    size="1em"
                    color="gray-600"/>
            </template>
        </q-input>
    </div>

    <div>
        <q-input v-model="user.login"
                 label="Почта или телефон"
                 outlined
                 stack-label
                 :dense="true">
            <template #prepend>
                <q-icon
                    name="mail"
                    size="1em"
                    color="gray-600"/>
            </template>
        </q-input>
    </div>

    <div>
        <q-input v-model="user.password"
                 outlined
                 stack-label
                 :dense="true"
                 label="Пароль"
                 :type="user_password_isHidden ? 'password' : 'text'">

            <template #prepend>
                <q-icon
                    name="password"
                    size="1em"
                    color="gray-600"/>
            </template>

            <template v-slot:append>
                <q-icon
                    :name="user_password_isHidden ? 'visibility-off' : 'visibility'"
                    class="cursor-pointer"
                    color="gray-600"
                    @click="user_password_isHidden = !user_password_isHidden"
                />
            </template>

        </q-input>
    </div>

</template>

<script>


export default {
    components: {},

    props: {
        user: {},
    },

    computed: {
        text() {
            if (this.user.id > 0) {
                return this.user.text
            } else {
                return "Гость"
            }
        },
        color() {
            if (this.user.id > 0) {
                return this.user.color
            } else {
                return "grey"
            }
        },
    },

    data() {
        return {
            user_password_isHidden: true,
        }
    },

}
</script>


<style>

</style>
