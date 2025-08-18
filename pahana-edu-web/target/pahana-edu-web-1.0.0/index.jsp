<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>Login — Pahana Edu</title>
<style>
  body{font-family:system-ui,Segoe UI,Roboto,Arial,sans-serif;background:#f7f7fb;margin:0;display:flex;align-items:center;justify-content:center;height:100vh}
  .box{background:#fff;border:1px solid #e5e7eb;border-radius:12px;width:360px;padding:24px}
  label{display:block;margin:8px 0 4px}
  input{width:100%;padding:10px;border:1px solid #d1d5db;border-radius:8px}
  .btn{margin-top:12px;width:100%;padding:10px;border:none;border-radius:8px;background:#0f4c81;color:#fff;font-weight:600;cursor:pointer}
  .err{color:#b00020;margin-bottom:8px}
</style>
</head><body>
  <div class="box">
    <h2 style="margin-top:0">Pahana Edu Login</h2>
    <form method="post" action="login">
      <div class="err"><%= request.getAttribute("error")!=null ? request.getAttribute("error") : "" %></div>
      <label>Username</label>
      <input name="username" required/>
      <label>Password</label>
      <input name="password" type="password" required/>
      <button class="btn">Login</button>
    </form>
    <p style="margin-top:12px;color:#555"><small>Use admin/admin123 or cashier/cashier123 (after running SQL seed).</small></p>
  </div>
</body></html>
