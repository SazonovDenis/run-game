<%@ page import="jandcode.core.apx.web.gsp.*; jandcode.commons.*; jandcode.core.*; jandcode.core.web.*; jandcode.core.web.gsp.*;jandcode.core.web.std.gsp.*;" %>
<!doctype html>
<%
  BaseGsp th = this
  th.request.disableCache()
  //
  def ctx = th.inst(JsIndexGspContext)
  def wpCtx = th.inst(FrontendIndexGspContext)
  //
  ctx.title = "Word strike"

  wpCtx.addLink("main")
%>
<html>
<head>
  <meta charset="UTF-8">
  <title>${ctx.title}!</title>

  <link rel="apple-touch-icon" href="${th.ref(th.path('./icon/apple-touch-icon.png'))}">
  <!-- 180x180 - ставим первым для safari -->
  <link rel="icon" href="${th.ref(th.path('./icon/favicon.ico'))}" sizes="any">
  <!-- 32x32 -->
  <link rel="icon" href="${th.ref(th.path('./icon/icon.svg'))}" type="image/svg+xml">
  <link rel="manifest" href="${th.ref(th.path('./icon/manifest.txt'))}">

  <meta name="viewport"
        content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">

  <style>
  @keyframes fade-splash {
    from {
      opacity: 0.1;
    }
    to {
      opacity: 1;
    }
  }

  @keyframes progress-bar {
    from {
      opacity: 1;
    }
    50% {
      opacity: 1;
    }
    to {
      opacity: 0;
    }
  }

  @keyframes progress-bar-bar {
    from {
      width: 0;
    }
    to {
      width: 100%;
    }
  }

  @keyframes fade-icon {
    0% {
      opacity: 0.1;
    }
    20% {
      opacity: 1;
    }
    80% {
      opacity: 1;
    }
    100% {
      opacity: 0.1;
    }
  }

  :root {
    --bar-delay: 5s;
    --bar-duration: 25s;
  }

  .jc-splash {
    text-align: center;
    font-family: serif, sans-serif;
    font-weight: lighter;
    padding-top: 5%;
    z-index: 1000000;
    animation-name: fade-splash;
    animation-duration: 4s;
  }

  .jc-splash-icon {
    display: flex;
    justify-content: center;
    padding: 1em;

    opacity: 0.7;
  }

  .jc-splash-icon img {
    width: 5rem;
    height: 5rem;

    animation-name: fade-icon;
    animation-duration: 2s;
    animation-iteration-count: infinite;
    display: none;
  }

  .jc-progress {
    display: flex;
    justify-content: center;
  }

  .jc-progress-bar {
    width: 8em;
    border: 1px silver solid;
    border-radius: 0.5rem;
    display: none;
    opacity: 0;
    animation-name: progress-bar;
    animation-delay: var(--bar-delay);
    animation-duration: var(--bar-duration);
  }

  .jc-progress-bar-bar {
    height: 4px;
    width: 100%;
    background: silver;
    animation-name: progress-bar-bar;
    animation-delay: var(--bar-delay);
    animation-duration: calc(var(--bar-duration) / 2);
    animation-timing-function: ease-out;
  }

  .jc-splash-footer {
    position: fixed;
    bottom: 5%;
    width: 100%;
    display: flex;
    justify-content: center;
    font-size: 1.0em;
    color: #3b3b3b;
  }
  </style>

</head>

<body>

<div id="jc-splash" class="jc-splash">

  <div style="font-size: 2em;">Игровой тренажер</div>

  <div style="font-size: 3em;">${ctx.title}</div>

  <div class="jc-splash-icon">
    <img id="jc-splash-icon" class="jc-splash-icon"
         src="${th.ref(th.path('./img/logo.svg'))}"/>
  </div>

  <div class="jc-progress">
    <div id="jc-progress-bar" class="jc-progress-bar">
      <div class="jc-progress-bar-bar">
      </div>
    </div>
  </div>

  <div class="jc-splash-footer">
    &copy; ТОО &laquo;Jadatex&raquo;, 2012-2024
  </div>

</div>

<script>
    let icon = document.getElementById("jc-splash-icon");
    let bar = document.getElementById("jc-progress-bar");
    icon.onload = function() {
        icon.style.display = "block";
        bar.style.display = "block";
    }

    window.addEventListener("load", function() {
        //document.getElementById("jc-splash").style.display = "none";
    })
</script>

<div id="jc-app"></div>
<% ctx.outLinks() %>
<script>
    document.getElementById("jc-splash").style.display = "none";
    ${wpCtx.libraryName}.run()
</script>
</body>
</html>