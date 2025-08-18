<!-- ===== Delete Confirm (drop-in) ===== -->



<style>
  .dc-overlay{position:fixed;inset:0;background:rgba(17,24,39,.55);display:none;
              align-items:center;justify-content:center;z-index:1000;}
  .dc-dialog{width:min(540px,92vw);background:#fff;border-radius:14px;padding:22px;
             box-shadow:0 20px 60px rgba(0,0,0,.25);position:relative}
  .dc-icon{width:64px;height:64px;border-radius:999px;background:#fee2e2;
           display:grid;place-items:center;color:#dc2626;font-size:30px;margin:0 auto 10px}
  .dc-title{margin:6px 0 4px;text-align:center;font-size:20px}
  .dc-text{color:#374151;text-align:center;margin:0 0 16px}
  .dc-actions{display:flex;gap:10px;justify-content:center}
  .dc-danger{background:#dc2626;border:1px solid #dc2626;color:#fff;
             padding:10px 16px;border-radius:10px;font-weight:700;cursor:pointer}
  .dc-cancel{background:#fff;border:1px solid #d1d5db;color:#111827;
             padding:10px 16px;border-radius:10px;cursor:pointer}
  .dc-close{position:absolute;right:8px;top:4px;border:0;background:none;
            font-size:22px;color:#6b7280;cursor:pointer}
</style>

<div id="dc-overlay" class="dc-overlay" role="dialog" aria-modal="true" aria-labelledby="dc-title">
  <div class="dc-dialog">
    <button class="dc-close" aria-label="Close" onclick="dcHide()">×</button>
    <div class="dc-icon">!</div>
    <h3 id="dc-title" class="dc-title">Are you sure?</h3>
    <p id="dc-text" class="dc-text">This action cannot be undone.</p>
    <div class="dc-actions">
      <button id="dc-yes" class="dc-danger">Yes, continue</button>
      <button type="button" class="dc-cancel" onclick="dcHide()">No, go back</button>
    </div>
  </div>
</div>

<script>
  // Tiny helper to show a pretty confirm and then submit the form
  (function(){
    let targetForm = null;

    window.showDeleteConfirm = function(form, entity){
      targetForm = form;
      const tr   = form.closest('tr');
      const name = (tr?.querySelector(entity==='item' ? '.item-name' : '.cust-name')?.textContent || '').trim();

      document.getElementById('dc-title').textContent =
        `Are you sure you want to delete this ${entity}?`;
      document.getElementById('dc-text').textContent =
        (name ? `"${name}" will be permanently deleted. This cannot be undone.` :
                 `Deleting this ${entity} cannot be undone.`);
      dcShow();
      return false; // stop normal submit until user confirms
    };

    function dcShow(){
      const ov = document.getElementById('dc-overlay');
      ov.style.display = 'flex';
      setTimeout(()=>document.getElementById('dc-yes').focus(),0);
    }
    window.dcHide = function(){
      document.getElementById('dc-overlay').style.display = 'none';
      targetForm = null;
    };

    document.getElementById('dc-yes').addEventListener('click', function(){
      const f = targetForm;
      dcHide();
      if (f) f.submit(); // submit original form
    });

    // click backdrop or press ESC to close
    document.getElementById('dc-overlay').addEventListener('click', (e)=>{
      if (e.target.id === 'dc-overlay') dcHide();
    });
    document.addEventListener('keydown', (e)=>{
      if (e.key === 'Escape') dcHide();
    });
  })();
</script>
<!-- ===== /Delete Confirm ===== -->
