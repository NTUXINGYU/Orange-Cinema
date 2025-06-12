<template>
    <div id="welcome">
        <div id="bk">Welcome to the Cinema Management System Backend</div>
    </div>
</template>

<script>
export default {
    name: "Welcome",
};
</script>

<style scoped>
#welcome {
    background: url("../assets/welcome.jpg");
    background-size: cover;
    height: 100%;
    width: 100%;
}
#bk {
    color: #409EFF;
    margin: 0;
    text-align: center;
    font-size: 36px;
    padding-top: 20px;
}
</style>
