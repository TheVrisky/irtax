const titles={overview:"نمای کلی",exams:"آزمون‌ها",articles:"مطالب",support:"پاسخ‌گویی",infographics:"اینفوگرافیک‌ها",users:"کاربران",categories:"دسته‌بندی‌ها",settings:"تنظیمات"};
const toast=document.getElementById("toast");let activeRequest=0;
const requests=[
{name:"محمد رضایی",topic:"در مورد مهلت ارسال صورتحساب",question:"سلام، برای ارسال صورتحساب الکترونیکی بعد از پایان مهلت چه راهکاری وجود دارد؟",status:"new"},
{name:"سارا احمدی",topic:"ابهام در ماده ۱۱",question:"تفاوت تبصره اول و دوم ماده ۱۱ را چطور باید در نظر بگیریم؟",status:"review"},
{name:"علی کریمی",topic:"ثبت حساب تجاری",question:"آیا برای هر شعبه باید حساب تجاری جداگانه معرفی شود؟",status:"answered"}];
function notify(message){toast.textContent=message;toast.classList.add("show");clearTimeout(notify.timer);notify.timer=setTimeout(()=>toast.classList.remove("show"),2500)}
function showPage(page){document.querySelectorAll(".page").forEach(el=>el.classList.toggle("active",el.id==="page-"+page));document.querySelectorAll("[data-page]").forEach(el=>el.classList.toggle("active",el.dataset.page===page));document.getElementById("pageTitle").textContent=titles[page]||"پنل مدیریت";window.scrollTo({top:0,behavior:"smooth"})}
document.querySelectorAll("[data-page]").forEach(el=>el.addEventListener("click",()=>showPage(el.dataset.page)));
document.getElementById("theme").addEventListener("click",()=>document.body.classList.toggle("light"));
document.querySelectorAll("[data-notice]").forEach(el=>el.addEventListener("click",()=>notify(el.dataset.notice)));
const dialog=document.getElementById("replyDialog");
function openRequest(index){activeRequest=index;const item=requests[index];document.getElementById("dialogTitle").textContent=item.topic;document.getElementById("fullQuestion").textContent=item.name+" می‌پرسد: "+item.question;document.getElementById("replyText").value="";dialog.showModal()}
document.querySelectorAll("[data-reply]").forEach(el=>el.addEventListener("click",()=>openRequest(Number(el.dataset.reply))));
document.getElementById("sendReply").addEventListener("click",()=>{const reply=document.getElementById("replyText").value.trim();if(!reply){notify("متن پاسخ را وارد کنید.");return}requests[activeRequest].status="answered";dialog.close();notify("پاسخ فقط در پیش‌نمایش ثبت شد و به سرور ارسال نشد.");const row=document.querySelectorAll(".support-row")[activeRequest];if(row){row.dataset.status="answered";const badge=row.querySelector(".status");badge.className="status answered";badge.textContent="پاسخ داده‌شده"}});
document.getElementById("markReview").addEventListener("click",()=>{requests[activeRequest].status="review";dialog.close();notify("وضعیت در پیش‌نمایش تغییر کرد.")});
document.getElementById("supportFilter").addEventListener("change",e=>document.querySelectorAll(".support-row").forEach(row=>row.hidden=e.target.value!=="all"&&row.dataset.status!==e.target.value));
document.getElementById("txtFile").addEventListener("change",e=>{const file=e.target.files[0],out=document.getElementById("filePreview");if(!file){out.textContent="هنوز فایلی انتخاب نشده است.";return}if(!file.name.toLowerCase().endsWith(".txt")){out.textContent="فقط فایل TXT پذیرفته می‌شود.";return}const reader=new FileReader();reader.onload=()=>{const value=String(reader.result||""),lines=value.split(/\r?\n/).filter(Boolean);out.textContent=file.name+" · "+file.size.toLocaleString("fa-IR")+" بایت · "+lines.length.toLocaleString("fa-IR")+" خط · "+value.slice(0,150).replace(/\s+/g," ")};reader.onerror=()=>out.textContent="خواندن فایل ناموفق بود.";reader.readAsText(file,"UTF-8")});
document.getElementById("articleSearch").addEventListener("input",e=>{const q=e.target.value.trim().toLocaleLowerCase("fa");document.querySelectorAll("#articleRows tr").forEach(row=>row.hidden=!row.textContent.toLocaleLowerCase("fa").includes(q))});
document.getElementById("newArticle").addEventListener("click",()=>notify("فرم ثبت مطلب پس از اتصال API اضافه می‌شود."));
