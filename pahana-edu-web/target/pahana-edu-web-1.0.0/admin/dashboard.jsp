<%@ page contentType="text/html; charset=UTF-8" %>
<%
    // Allow only admins
    String role = (String) session.getAttribute("role");
    if (role == null || !"ADMIN".equals(role)) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    String ctx = request.getContextPath();
    String username = (String) session.getAttribute("username");
    if (username == null) username = "Admin";
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>Pahana Edu — Admin</title>
<style>
  :root{
    --bg:#f6f8fb; --panel:#ffffff; --shadow:0 8px 24px rgba(16,24,40,.08);
    --brand:#0f4c81; --brand-2:#1e6fb8; --muted:#667085; --line:#e5e7eb;
    --sidebar:#121826; --sidebar-2:#1a2435; --sidebar-text:#cbd5e1;
  }
  *{box-sizing:border-box}
  body{margin:0;background:var(--bg);font-family:system-ui,Segoe UI,Roboto,Arial,sans-serif;color:#0b1220}
  a{color:inherit;text-decoration:none}
  .shell{display:grid;grid-template-columns:260px 1fr;min-height:100vh}
  /* Sidebar */
  .sidebar{background:var(--sidebar);color:var(--sidebar-text);display:flex;flex-direction:column}
  .brand{display:flex;align-items:center;gap:.6rem;padding:18px;border-bottom:1px solid rgba(255,255,255,.06)}
  .brand .logo{width:28px;height:28px;border-radius:6px;background:linear-gradient(135deg,#3aa0ff,#0f4c81);box-shadow:inset 0 1px 0 rgba(255,255,255,.3)}
  .brand .title{color:#fff;font-weight:700;letter-spacing:.3px}
  .nav{padding:12px 8px;flex:1;overflow:auto}
  .nav a{display:flex;align-items:center;gap:10px;padding:10px 12px;border-radius:10px;margin:4px 8px;color:var(--sidebar-text)}
  .nav a.active{background:var(--sidebar-2);color:#fff}
  .nav a:hover{background:rgba(255,255,255,.06)}
  .nav .ico{width:18px;height:18px;display:inline-block;opacity:.95}
  .sidebar .foot{padding:14px 16px;border-top:1px solid rgba(255,255,255,.06);display:flex;justify-content:space-between;align-items:center}
  .btn-out{padding:6px 10px;border:1px solid rgba(255,255,255,.25);border-radius:8px;color:#fff}
  /* Main */
  .main{display:flex;flex-direction:column}
  .topbar{background:var(--panel);box-shadow:var(--shadow);display:flex;align-items:center;gap:16px;padding:14px 18px;position:sticky;top:0;z-index:10}
  .topbar .title{font-weight:700;color:var(--brand)}
  .search{flex:1;display:flex;align-items:center;background:#f2f4f7;border:1px solid var(--line);border-radius:10px;padding:8px 12px}
  .search input{border:0;outline:0;background:transparent;width:100%}
  .user{display:flex;align-items:center;gap:10px;margin-left:auto}
  .avatar{width:32px;height:32px;border-radius:50%;background:linear-gradient(135deg,#7cc0ff,#2d7be0);color:#fff;display:grid;place-items:center;font-weight:700}
  .role{font-size:12px;color:var(--muted)}
  .wrap{padding:22px}
  .tiles{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:16px}
  .tile{background:var(--panel);border:1px solid var(--line);border-radius:14px;padding:18px;box-shadow:var(--shadow);display:flex;flex-direction:column;gap:14px;transition:transform .08s, box-shadow .08s}
  .tile:hover{transform:translateY(-2px);box-shadow:0 14px 32px rgba(16,24,40,.12)}
  .tile .icon{width:44px;height:44px;border-radius:12px;display:grid;place-items:center;color:#fff;font-size:22px;font-weight:800}
  .tile .name{font-weight:700}
  .muted{color:var(--muted);font-size:13px}
  .i1{background:linear-gradient(135deg,#ff7ab6,#ff3f7a)}
  .i2{background:linear-gradient(135deg,#22c55e,#16a34a)}
  .i3{background:linear-gradient(135deg,#6366f1,#4338ca)}
  .i4{background:linear-gradient(135deg,#f97316,#ea580c)}
  .i5{background:linear-gradient(135deg,#06b6d4,#0891b2)}
  .i6{background:linear-gradient(135deg,#f43f5e,#e11d48)}
</style>
</head>
<body>

<div class="shell">
  <!-- Sidebar -->
  <aside class="sidebar">
    <div class="brand">
      <div class="logo"></div>
      <div class="title">PAHANA EDU</div>
    </div>
    <nav class="nav">
      <a class="active" href="<%=ctx%>/admin/dashboard.jsp"><span class="ico">🏠</span> Home</a>
      <a href="<%=ctx%>/customers"><span class="ico">👥</span> Customers</a>
      <a href="<%=ctx%>/items"><span class="ico">📦</span> Items</a>
      <a href="<%=ctx%>/billing"><span class="ico">🧾</span> Billing</a>
      <a href="<%=ctx%>/admin/reports"><span class="ico">📈</span> Sales Reports</a>
      <a href="<%=ctx%>/admin/customer-report"><span class="ico">🧾</span> Customer Bill Report</a>
      <a href="<%=ctx%>/help.jsp"><span class="ico">❓</span> Help</a>
    </nav>
    <div class="foot">
      <span class="role">Admin</span>
      <a class="btn-out" href="<%=ctx%>/logout">Logout</a>
    </div>
  </aside>

  <!-- Main -->
  <div class="main">
    <div class="topbar">
      <div class="title">Admin</div>
      <div class="search"><input placeholder="Search… (UI only)"></div>
      <div class="user">
        <div class="role">Welcome, <%= username %></div>
        <div class="avatar"><%= username.substring(0,1).toUpperCase() %></div>
      </div>
    </div>

    <div class="wrap">
      <div class="tiles">
        <!-- Customers -->
        <a class="tile" href="<%=ctx%>/customers">
          <div class="icon i1">👥</div>
          <div class="name">Customers</div>
          <div class="muted">Create, update, delete</div>
        </a>

        <!-- Items -->
        <a class="tile" href="<%=ctx%>/items">
          <div class="icon i2">📦</div>
          <div class="name">Items</div>
          <div class="muted">Manage inventory & prices</div>
        </a>

        <!-- Billing -->
        <a class="tile" href="<%=ctx%>/billing">
          <div class="icon i3">🧾</div>
          <div class="name">Billing</div>
          <div class="muted">Create invoices</div>
        </a>

        <!-- Sales Reports -->
        <a class="tile" href="<%=ctx%>/admin/reports">
          <div class="icon i4">📈</div>
          <div class="name">Sales Reports</div>
          <div class="muted">Daily/period summaries</div>
        </a>

        <!-- Customer Bill Report -->
        <a class="tile" href="<%=ctx%>/admin/customer-report" title="View bills for a selected customer">
          <div class="icon i5" aria-hidden="true">🧾</div>
          <div class="name">Customer Bill Report</div>
          <div class="muted">Bills by customer</div>
        </a>

        <!-- HELP (moved here, before Settings) -->
        <a class="tile" href="<%=ctx%>/help.jsp">
          <div class="icon i5">📘</div>
          <div class="name">Help</div>
          <div class="muted">How to use Pahana Edu</div>
        </a>

        <!-- Settings -->
        <a class="tile" href="<%=ctx%>/settings.jsp">
          <div class="icon i6">⚙️</div>
          <div class="name">Settings</div>
          <div class="muted">Preferences</div>
        </a>
      </div>
    </div>
  </div>
</div>

</body>
</html>
