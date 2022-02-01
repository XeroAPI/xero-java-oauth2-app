<html class="no-js">
<head>
    <link rel="stylesheet" href="css/main.css"/>
    <link rel="stylesheet" href="css/font.css"/>
    <link rel="stylesheet" href="css/complement.css"/>
    <script>(function (e, t, n) {
        var r = e.querySelectorAll("html")[0];
        r.className = r.className.replace(/(^|\s)no-js(\s|$)/, "$1js$2")
    })(document, window, 0);</script>
</head>
<body>
<div class="container" role="main">
    <h1 class="font-weight-bold">Select your Camt.053/054 file</h1>
    <form method="post" action="upload" enctype="multipart/form-data" class="box">
        <div class="box__input">
            <svg class="box__icon" xmlns="http://www.w3.org/2000/svg" width="50" height="43" viewBox="0 0 50 43">
                <path d="M48.4 26.5c-.9 0-1.7.7-1.7 1.7v11.6h-43.3v-11.6c0-.9-.7-1.7-1.7-1.7s-1.7.7-1.7 1.7v13.2c0 .9.7 1.7 1.7 1.7h46.7c.9 0 1.7-.7 1.7-1.7v-13.2c0-1-.7-1.7-1.7-1.7zm-24.5 6.1c.3.3.8.5 1.2.5.4 0 .9-.2 1.2-.5l10-11.6c.7-.7.7-1.7 0-2.4s-1.7-.7-2.4 0l-7.1 8.3v-25.3c0-.9-.7-1.7-1.7-1.7s-1.7.7-1.7 1.7v25.3l-7.1-8.3c-.7-.7-1.7-.7-2.4 0s-.7 1.7 0 2.4l10 11.6z"/>
            </svg>
            <input type="file" name="file" id="file" class="box__file"/>
            <label for="file">
                <strong>Choose a file</strong>
                <span class="box__dragndrop"> or drag it here</span>
            </label>
            <button type="submit" class="box__button">Upload</button>
        </div>
        <div class="box__uploading">Uploading&hellip;</div>
        <div class="box__success">Done!</div>
        <div class="box__error">Error!.</div>
    </form>
</div>
<script src="js/main.js"></script>
<script defer src="https://static.cloudflareinsights.com/beacon.min.js/v652eace1692a40cfa3763df669d7439c1639079717194"
        integrity="sha512-Gi7xpJR8tSkrpF7aordPZQlW2DLtzUlZcumS8dMQjwDHEnw9I7ZLyiOj/6tZStRBGtGgN6ceN6cMH8z7etPGlw=="
        data-cf-beacon='{"rayId":"6cc527411be5da86","token":"2863ac31b15c48f5b94bd8b0c38aba94","version":"2021.12.0","si":100}'
        crossorigin="anonymous"></script>
</body>
</html>