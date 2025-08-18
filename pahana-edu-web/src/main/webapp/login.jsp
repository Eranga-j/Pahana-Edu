<%@ page contentType="text/html; charset=UTF-8" %>
<%
  String ctx = ((jakarta.servlet.http.HttpServletRequest)request).getContextPath();
  String error = (String) request.getAttribute("error"); // optional
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8" />
<title>Pahana Edu – Login</title>
<meta name="viewport" content="width=device-width, initial-scale=1" />
<style>
  :root{
    --brand:#0f4c81;       /* match admin palette */
    --bg:#eaf0f6;          /* page bg like the sample */
    --card:#e9eef4;        /* soft panel for neumorphism */
    --muted:#6b7280;
  }
  *{box-sizing:border-box}
  html,body{height:100%}
  body{
    margin:0;
    font-family:system-ui, Segoe UI, Roboto, Arial, sans-serif;
    color:#0b1220;
    background:
      radial-gradient(900px 420px at 15% 10%, rgba(15,76,129,.06), transparent),
      var(--bg);
    display:grid; place-items:center; padding:24px;
  }

  /* Neumorphic card */
  .card{
    width:min(92vw,440px);
    background:var(--card);
    border-radius:28px;
    padding:28px 28px 24px;
    box-shadow:
      18px 18px 36px rgba(0,0,0,.12),
      -12px -12px 28px rgba(255,255,255,.75);
  }

  /* Title only (no logo) */
  .brand{
    text-align:center; margin:6px 0 18px;
  }
  .brand h1{
    margin:4px 0 0;
    font-size:22px; letter-spacing:.3px; color:#111827;
  }
  .brand p{
    margin:6px 0 0; color:var(--muted); font-size:13px;
  }

  .error{
    background:#ffe9e9; color:#8a1f1f; border:1px solid #ffb4b4;
    padding:10px 12px; border-radius:12px; margin-bottom:12px; font-size:13px;
  }

  .field{margin:14px 0}
  .label{font-size:13px; color:#4b5563; margin:0 0 6px 6px}
  .input{
    display:flex; align-items:center; gap:10px;
    background:var(--card);
    border-radius:14px; padding:12px 14px;
    border:none; outline:none; width:100%;
    box-shadow:
      inset 6px 6px 12px rgba(0,0,0,.08),
      inset -6px -6px 12px rgba(255,255,255,.85);
  }
  .input input{
    border:0; outline:0; background:transparent; width:100%; font-size:15px;
  }
  .ico{width:18px}

  .btn{
    width:100%; padding:12px 16px; margin-top:12px;
    border-radius:14px; border:1px solid #1e6fb8;
    background:linear-gradient(180deg,#6bb6ff 0%, #2d7be0 45%, #1a5fbe 100%);
    color:#fff; font-weight:700; letter-spacing:.3px; cursor:pointer;
    box-shadow:
      inset 0 1px 0 rgba(255,255,255,.7),
      inset 0 -2px 6px rgba(0,0,0,.25),
      0 8px 18px rgba(27,69,123,.25);
  }
  .btn:hover{filter:brightness(1.05)}
  .btn:active{transform:translateY(1px)}

  .foot{
    text-align:center; margin-top:12px; color:var(--muted); font-size:13px;
  }
</style>
</head>
<body>

  <form class="card" method="post" action="<%=ctx%>/login">
    <div class="brand">
      <h1>Pahana Edu</h1>
      <!-- remove the old "Web Development / Made easy!" and logo -->
    </div>

    <% if (error != null && !error.isBlank()) { %>
      <div class="error"><%= error %></div>
    <% } %>

    <div class="field">
      <div class="label">Username</div>
      <div class="input">
        <span class="ico">👤</span>
        <input name="username" autocomplete="username" placeholder="username" required />
      </div>
    </div>

    <div class="field">
      <div class="label">Password</div>
      <div class="input">
        <span class="ico">🔒</span>
        <input name="password" type="password" autocomplete="current-password" placeholder="password" required />
      </div>
    </div>

    <button class="btn">Login</button>

    
  </form>

</body>
</html>
