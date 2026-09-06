const $=s=>document.querySelector(s);
const modules=[
{id:'malware',icon:'🦠',name:'Malware Defense',desc:'Threat detection engine'},
{id:'scanner',icon:'🔍',name:'File & APK Scanner',desc:'On-demand analysis'},
{id:'ai',icon:'🧠',name:'AI Risk Analysis',desc:'Heuristic risk scoring'},
{id:'web',icon:'🌐',name:'Web Protection',desc:'Phishing risk checks'},
{id:'realtime',icon:'⚡',name:'Real-Time Protection',desc:'Security event framework'},
{id:'cloud',icon:'☁️',name:'Cloud Intelligence',desc:'Reputation architecture'},
{id:'privacy',icon:'🔒',name:'Privacy Shield',desc:'Privacy controls'},
{id:'quarantine',icon:'🗃️',name:'Quarantine System',desc:'Threat isolation records'}
];
const privacy=[{id:'permissions',icon:'🔐',name:'Permission Guard',desc:'Sensitive permission controls'},{id:'tracking',icon:'👁️',name:'Tracking Protection',desc:'Privacy risk architecture'},{id:'data',icon:'🗄️',name:'Data Privacy Shield',desc:'Sensitive data controls'}];
const threatDB=[
{keys:['trojan','backdoor','virus'],type:'Trojan',severity:'High',score:91,action:'Quarantine the suspected item and run a full security review.'},
{keys:['ransomware','encryptor','locker'],type:'Ransomware',severity:'Critical',score:99,action:'Quarantine immediately and disconnect from untrusted networks.'},
{keys:['spyware','stalker','monitor'],type:'Spyware',severity:'High',score:86,action:'Remove or quarantine and review installed applications.'},
{keys:['adware','popup','aggressive.ads'],type:'Adware',severity:'Medium',score:63,action:'Review and remove the unwanted application.'},
{keys:['badsite.test','phishing','fake-login'],type:'Phishing',severity:'High',score:88,action:'Do not enter credentials; verify the official domain independently.'}
];
function get(k,d){try{return JSON.parse(localStorage.getItem(k))??d}catch{return d}}
let state=get('shieldV10Modules',Object.fromEntries(modules.map(x=>[x.id,true])));
let pstate=get('shieldV10Privacy',Object.fromEntries(privacy.map(x=>[x.id,true])));
let activity=get('shieldV10Activity',[]), quarantine=get('shieldV10Quarantine',[]), stats=get('shieldV10Stats',{scans:0,threats:0,web:0});
function save(){localStorage.setItem('shieldV10Modules',JSON.stringify(state));localStorage.setItem('shieldV10Privacy',JSON.stringify(pstate));localStorage.setItem('shieldV10Activity',JSON.stringify(activity));localStorage.setItem('shieldV10Quarantine',JSON.stringify(quarantine));localStorage.setItem('shieldV10Stats',JSON.stringify(stats))}
function score(){return Math.round(35+Object.values(state).filter(Boolean).length*6+Object.values(pstate).filter(Boolean).length*5)}
function addEvent(type,msg,icon='🛡️'){activity.unshift({type,msg,icon,time:new Date().toLocaleString()});activity=activity.slice(0,50);save();renderActivity()}
function showPage(id){document.querySelectorAll('.page').forEach(x=>x.classList.remove('active'));$('#'+id).classList.add('active');document.querySelectorAll('nav button').forEach(b=>b.classList.toggle('active',b.dataset.page===id));window.scrollTo({top:0,behavior:'smooth'});if(id==='quarantine')renderQuarantine()}
function color(s){return s>=80?'#ff5c68':s>=45?'#ffc94f':'#40ec82'}
function findThreat(q){q=q.toLowerCase();return threatDB.find(t=>t.keys.some(k=>q.includes(k)))}
function renderHome(){
let n=score(),active=Object.values(state).filter(Boolean).length,grade=n>=90?'A':n>=75?'B':n>=55?'C':'D';
$('#score').innerHTML=n+'<small>/100</small>';$('#heroScore').textContent=n;$('#grade').textContent=grade;$('#scoreFill').style.width=n+'%';
$('#heroTitle').textContent=n>=90?'System Protected':n>=70?'Protection Reduced':'Security Needs Attention';$('#heroText').textContent=n>=90?'Brightcell Shield security platform is active.':'Enable more protection controls to improve coverage.';
$('#statScans').textContent=stats.scans;$('#statThreats').textContent=stats.threats;$('#statQuarantine').textContent=quarantine.length;$('#statWeb').textContent=stats.web;$('#moduleStatus').textContent=active+'/'+modules.length+' ACTIVE';
$('#moduleSummary').innerHTML=modules.slice(0,5).map(m=>`<div class="module"><div><div class="module-icon">${m.icon}</div><div><b>${m.name}</b><small>${m.desc}</small></div></div><strong>${state[m.id]?'ACTIVE':'OFF'}</strong></div>`).join('');
$('#homeActivity').innerHTML=activity.slice(0,3).length?activity.slice(0,3).map(eventHtml).join(''):'<p class="muted">No security activity yet.</p>';
}
function eventHtml(e){return `<div class="event"><div class="event-icon">${e.icon}</div><div><b>${e.type}</b><span>${e.msg}</span><small>${e.time}</small></div></div>`}
function renderActivity(){$('#activityList').innerHTML=activity.length?activity.map(eventHtml).join(''):'<p class="muted">No security activity yet.</p>';renderHome()}
function runScan(){
let q=$('#scanInput').value.trim();if(!q)return toast('Enter a file or indicator');
$('#scanProgress').classList.remove('hidden');$('#scanResult').classList.add('hidden');let p=0;
let timer=setInterval(()=>{p=Math.min(100,p+Math.floor(Math.random()*15)+8);$('#scanPct').textContent=p+'%';$('#scanFill').style.width=p+'%';$('#scanLabel').textContent=p<30?'Preparing threat engine...':p<65?'Running AI risk analysis...':p<90?'Checking intelligence indicators...':'Finalizing result...';$('#scanDetail').textContent=q;if(p>=100){clearInterval(timer);finishScan(q)}},140)
}
function demoScan(){$('#scanInput').value='ransomware.apk';runScan()}
function finishScan(q){
$('#scanProgress').classList.add('hidden');let t=findThreat(q);stats.scans++;
let r=t||{type:'No Known Threat',severity:'Low',score:8,action:'No matching indicator found in the demonstration engine.'};if(t){stats.threats++;quarantine.unshift({name:q,type:t.type,time:new Date().toLocaleString()});quarantine=quarantine.slice(0,30);addEvent('THREAT DETECTED',`${t.type} indicator found in ${q}`,'⚠️')}else addEvent('SCAN COMPLETE',`${q} completed local demonstration analysis.`,'🔍');
save();showScanResult(q,r,!!t);renderHome();toast(t?'Threat moved to quarantine':'Scan completed')
}
function showScanResult(q,r,threat){
let c=color(r.score);$('#scanResult').classList.remove('hidden');$('#scanResult').innerHTML=`<div class="result-top"><div class="risk-icon" style="background:${threat?'#3b1820':'#153d25'};color:${c}">${threat?'⚠️':'✓'}</div><div><small>SCAN RESULT</small><h3 style="color:${c}">${threat?r.severity.toUpperCase()+' RISK':'NO THREAT MATCH'}</h3><p>${r.type}</p></div></div><div class="risk-meter"><div><span>AI Risk Score</span><b style="color:${c}">${r.score}/100</b></div><div class="bar"><i style="width:${r.score}%;background:${c}"></i></div></div><div class="detail"><span>ITEM</span><b>${q}</b></div><div class="detail"><span>THREAT TYPE</span><b>${r.type}</b></div><div class="detail"><span>RECOMMENDED ACTION</span><b>${r.action}</b></div>`}
function renderQuarantine(){$('#quarantineList').innerHTML=quarantine.length?quarantine.map((x,i)=>`<div class="quarantine-item"><div class="item-icon">🗃️</div><div><b>${x.name}</b><small>${x.type} • ${x.time}</small></div><div class="item-actions"><button onclick="removeQ(${i})">REMOVE</button></div></div>`).join(''):'<p class="muted">Quarantine is empty.</p>'}
window.removeQ=i=>{quarantine.splice(i,1);save();renderQuarantine();renderHome();toast('Record removed from quarantine')}
function clearQuarantine(){quarantine=[];save();renderQuarantine();renderHome();toast('Quarantine cleared')}
function checkUrl(){
let q=$('#urlInput').value.trim();if(!q)return toast('Enter a URL or domain');let t=findThreat(q);stats.web++;save();
let r=t||{type:'No Known Phishing Match',severity:'Low',score:10,action:'No matching demonstration phishing indicator found. Continue using normal caution.'};let c=color(r.score);
$('#urlResult').classList.remove('hidden');$('#urlResult').innerHTML=`<div class="result-top"><div class="risk-icon" style="background:${t?'#3b1820':'#153d25'};color:${c}">${t?'⚠️':'✓'}</div><div><small>WEB ANALYSIS</small><h3 style="color:${c}">${t?'SUSPICIOUS':'LOW RISK MATCH'}</h3><p>${r.type}</p></div></div><div class="risk-meter"><div><span>Web Risk Score</span><b style="color:${c}">${r.score}/100</b></div><div class="bar"><i style="width:${r.score}%;background:${c}"></i></div></div><div class="detail"><span>DOMAIN / URL</span><b>${q}</b></div><div class="detail"><span>RECOMMENDATION</span><b>${r.action}</b></div>`;
addEvent('WEB CHECK COMPLETE',`${q} checked for phishing indicators.`,'🌐');renderHome()
}
function lookupIntel(){let q=$('#intelInput').value.trim();if(!q)return toast('Enter a threat indicator');let t=findThreat(q)||{type:'No Match',severity:'Low',score:5,action:'No matching record in the local demonstration intelligence database.'};let c=color(t.score);$('#intelResult').classList.remove('hidden');$('#intelResult').innerHTML=`<div class="result-top"><div class="risk-icon" style="background:#11281a;color:${c}">${t.score>60?'⚠️':'☁️'}</div><div><small>INTELLIGENCE RESULT</small><h3>${t.type}</h3><p>${t.severity} reputation classification</p></div></div><div class="risk-meter"><div><span>Reputation Risk</span><b style="color:${c}">${t.score}/100</b></div><div class="bar"><i style="width:${t.score}%;background:${c}"></i></div></div><div class="detail"><span>RECOMMENDED ACTION</span><b>${t.action}</b></div>`;addEvent('CLOUD INTELLIGENCE LOOKUP',`${q} checked against demonstration intelligence.`,'☁️')}
function syncIntel(){let p=0;$('#intelFill').style.width='0%';$('#intelText').textContent='Synchronizing intelligence architecture...';let t=setInterval(()=>{p+=10;$('#intelFill').style.width=p+'%';if(p>=100){clearInterval(t);$('#intelText').textContent='Threat intelligence cache synchronized (prototype data).';addEvent('INTELLIGENCE SYNC','Threat intelligence cache refreshed.','☁️');toast('Cloud intelligence sync complete')}},120)}
function renderFeed(){$('#feedList').innerHTML=[['🧬','Threat Signatures Updated','Demonstration indicators refreshed.'],['🎣','Phishing Pattern Pack','Credential-risk patterns available.'],['📡','Reputation Engine Ready','Local cache and cloud architecture active.']].map(x=>`<div class="feed-item"><div class="feed-icon">${x[0]}</div><div><b>${x[1]}</b><p>${x[2]}</p><small>Latest update</small></div></div>`).join('')}
function renderSecurity(){
$('#controls').innerHTML=modules.map(m=>`<div class="control"><div class="control-info"><div class="control-icon">${m.icon}</div><div><b>${m.name}</b><small>${m.desc}</small></div></div><button class="switch ${state[m.id]?'on':''}" onclick="toggle('m','${m.id}')"><i></i></button></div>`).join('');
$('#privacyControls').innerHTML=privacy.map(m=>`<div class="control"><div class="control-info"><div class="control-icon">${m.icon}</div><div><b>${m.name}</b><small>${m.desc}</small></div></div><button class="switch ${pstate[m.id]?'on':''}" onclick="toggle('p','${m.id}')"><i></i></button></div>`).join('');
let n=score(),a=Object.values(state).filter(Boolean).length,p=Object.values(pstate).filter(Boolean).length,issues=(modules.length-a)+(privacy.length-p);
$('#secScore').textContent=n+'/100';$('#secFill').style.width=n+'%';$('#enabledCount').textContent=a+'/'+modules.length;$('#privacyCount').textContent=p+'/'+privacy.length;$('#issueCount').textContent=issues+' ISSUE'+(issues===1?'':'S');
let rec=[];modules.filter(m=>!state[m.id]).forEach(m=>rec.push(`Enable ${m.name}`));privacy.filter(m=>!pstate[m.id]).forEach(m=>rec.push(`Enable ${m.name}`));$('#recommendations').innerHTML=rec.length?rec.map(x=>`<div class="recommendation"><div>⚠️</div><div><b>${x}</b><p>This security control is currently disabled.</p></div></div>`).join(''):`<div class="recommendation"><div>✓</div><div><b>Excellent Security Posture</b><p>All configured security and privacy controls are active.</p></div></div>`;
let vals=[62,70,76,82,86,91,n];$('#chart').innerHTML=vals.map(v=>`<i style="height:${Math.min(100,v)}%"></i>`).join('');
}
window.toggle=(g,id)=>{let o=g==='m'?state:pstate;o[id]=!o[id];save();renderSecurity();renderHome();addEvent(o[id]?'CONTROL ENABLED':'CONTROL DISABLED',id,o[id]?'🟢':'⚪')}
function analyzeSecurity(){let n=score();$('#riskMetric').textContent=n>=85?'Low':n>=65?'Moderate':'High';$('#coverageMetric').textContent=Math.round(Object.values(state).filter(Boolean).length/modules.length*100)+'%';$('#privacyMetric').textContent=Object.values(pstate).filter(Boolean).length===privacy.length?'Strong':'Needs Review';addEvent('ADVANCED ANALYSIS',`Security posture analyzed: ${n}/100.`,'📊');toast('Advanced security analysis complete')}
function clearActivity(){activity=[];save();renderActivity();toast('Activity cleared')}
function toast(m){$('#toast').textContent=m;$('#toast').classList.add('show');setTimeout(()=>$('#toast').classList.remove('show'),2200)}
renderHome();renderSecurity();renderQuarantine();renderFeed();if('serviceWorker'in navigator)navigator.serviceWorker.register('sw.js').catch(()=>{});
  
