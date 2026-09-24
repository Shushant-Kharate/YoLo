/** The complete OrbitGuard browser interface, embedded as one Java text block. */
public final class Frontend {
    private Frontend() {
    }

    public static final String PAGE = """
            <!doctype html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width,initial-scale=1">
              <title>OrbitGuard</title>
              <script src="https://cdn.jsdelivr.net/npm/onnxruntime-web@1.30.0/dist/ort.min.js"></script>
              <style>
                :root{color-scheme:dark;--bg:#071017;--panel:#0d1a22;--line:#203540;--text:#eef7fa;--muted:#91a9b5;--cyan:#70daf2;--amber:#f7b84b;--green:#77df98;--red:#ff7272}
                *{box-sizing:border-box}html{scroll-behavior:smooth}body{margin:0;background:radial-gradient(circle at 80% 0,#102936 0,transparent 35%),var(--bg);color:var(--text);font:15px/1.5 system-ui,-apple-system,Segoe UI,sans-serif}
                button,input{font:inherit}button{cursor:pointer}.shell{width:min(1180px,calc(100% - 32px));margin:auto}.topbar{position:sticky;top:0;z-index:5;background:#071017e8;border-bottom:1px solid var(--line);backdrop-filter:blur(16px)}
                .nav{height:68px;display:flex;align-items:center;justify-content:space-between}.brand{font-size:24px;font-weight:800;letter-spacing:-.5px}.brand b{color:var(--cyan)}.badge{border:1px solid #315161;border-radius:999px;padding:7px 12px;color:var(--cyan);font-size:12px}
                main{padding:28px 0 44px}.hero{margin-bottom:22px}.hero h1{font-size:clamp(30px,5vw,54px);line-height:1.02;margin:0 0 10px;letter-spacing:-2px}.hero p{max-width:700px;color:var(--muted);margin:0}
                .grid{display:grid;grid-template-columns:330px 1fr;gap:16px}.panel{background:linear-gradient(145deg,#0f2029,#0b171e);border:1px solid var(--line);border-radius:16px;box-shadow:0 18px 50px #0005}.controls{padding:20px}.controls h2,.planner h2{margin:0 0 5px;font-size:20px}.sub{color:var(--muted);font-size:13px;margin:0 0 18px}
                .drop{display:block;border:1px dashed #46616e;border-radius:12px;padding:24px 14px;text-align:center;background:#09151b;margin-bottom:18px}.drop input{width:100%;margin-top:12px}.row{display:flex;align-items:center;justify-content:space-between;gap:12px;margin:12px 0}.row output{font-weight:800;color:var(--cyan)}input[type=range]{width:100%;accent-color:var(--cyan)}
                .primary{width:100%;border:0;border-radius:10px;padding:12px 16px;background:var(--cyan);color:#061016;font-weight:800;margin-top:14px}.primary:disabled{opacity:.45;cursor:not-allowed}.status{display:flex;align-items:center;gap:8px;margin-top:16px;color:var(--muted);font-size:12px}.dot{width:9px;height:9px;border-radius:50%;background:var(--amber);box-shadow:0 0 12px currentColor}.dot.ready{background:var(--green)}.dot.error{background:var(--red)}
                .viewer{padding:12px;min-height:480px;display:grid;place-items:center;overflow:hidden}.viewer canvas{display:block;max-width:100%;max-height:620px;border-radius:10px;background:#03080b}.viewer canvas[hidden]{display:none}.empty{color:var(--muted);text-align:center}.empty strong{display:block;color:var(--text);font-size:18px;margin-bottom:5px}
                .results{grid-column:1/-1;padding:18px}.results-head{display:flex;justify-content:space-between;align-items:center}.results h2{font-size:18px;margin:0}.objects{display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:10px;margin-top:12px}.object{border:1px solid var(--line);border-left:3px solid var(--cyan);border-radius:10px;padding:12px;background:#09151b}.object.debris{border-left-color:var(--amber)}.object strong{display:flex;justify-content:space-between}.object small{color:var(--muted)}
                .planner{margin-top:16px;padding:20px}.planner-head{display:flex;justify-content:space-between;gap:14px;align-items:flex-start}.route-status{color:var(--green);border:1px solid #2d5940;border-radius:999px;padding:7px 12px;white-space:nowrap}.plan-layout{display:grid;grid-template-columns:210px 1fr;gap:16px;margin-top:16px}.legend{border:1px solid var(--line);border-radius:12px;padding:15px;color:var(--muted)}.legend p{margin:8px 0}.reset{width:100%;padding:9px;border:1px solid #39515d;color:var(--text);background:transparent;border-radius:8px;margin-top:12px}
                .orbit-grid{display:grid;grid-template-columns:repeat(20,1fr);gap:2px;background:#071116;border:1px solid var(--line);border-radius:12px;padding:10px;min-height:300px}.cell{border:0;border-radius:3px;background:#10212a;min-width:0}.cell:hover{background:#1a3542}.cell.path{background:#22586a}.cell.obstacle{background:var(--amber);box-shadow:0 0 10px #f7b84b66}.cell.start{background:var(--cyan);box-shadow:0 0 12px #70daf288}.cell.goal{background:var(--green);box-shadow:0 0 12px #77df9888}
                footer{border-top:1px solid var(--line);padding:20px 0;color:var(--muted);font-size:12px}.foot{display:flex;justify-content:space-between;gap:16px}
                @media(max-width:800px){.grid,.plan-layout{grid-template-columns:1fr}.results{grid-column:auto}.viewer{min-height:320px}.planner-head,.foot{flex-direction:column}.orbit-grid{min-height:220px}.badge{display:none}}
              </style>
            </head>
            <body>
              <header class="topbar"><div class="shell nav"><div class="brand">Orbit<b>Guard</b></div><div class="badge">5-file Java edition</div></div></header>
              <main class="shell">
                <section class="hero"><h1>Satellite and debris intelligence</h1><p>Run the included YOLOv8 ONNX model locally in your browser, inspect confidence, and calculate a collision-free route with Java A*.</p></section>
                <section class="grid">
                  <aside class="panel controls">
                    <h2>Analyze an image</h2><p class="sub">The image stays on this computer.</p>
                    <label class="drop"><strong>Choose a satellite image</strong><input id="file" type="file" accept="image/png,image/jpeg"></label>
                    <div class="row"><span>Confidence threshold</span><output id="thresholdValue">0.50</output></div>
                    <input id="threshold" type="range" min="0.20" max="0.95" step="0.05" value="0.50">
                    <button id="analyze" class="primary" disabled>Run model</button>
                    <div class="status"><span id="modelDot" class="dot"></span><span id="modelStatus">Loading model…</span></div>
                  </aside>
                  <div class="panel viewer" id="viewer"><div class="empty" id="empty"><strong>No image selected</strong>Choose a JPG or PNG to begin.</div><canvas id="canvas" hidden></canvas></div>
                  <section class="panel results"><div class="results-head"><h2>Detected objects</h2><span id="count">0</span></div><div class="objects" id="objects"><div class="empty">Results will appear here.</div></div></section>
                </section>
                <section class="panel planner">
                  <div class="planner-head"><div><h2>Java A* collision-avoidance planning</h2><p class="sub">Click cells to add or remove debris obstacles.</p></div><div class="route-status" id="routeStatus">Calculating…</div></div>
                  <div class="plan-layout"><aside class="legend"><p><b id="blockedCount">0</b> blocked cells</p><p><b id="pathCost">0</b> path cost</p><p>Cyan: start</p><p>Green: goal</p><p>Amber: debris</p><button class="reset" id="reset">Reset plan</button></aside><div class="orbit-grid" id="orbitGrid" aria-label="Interactive planning grid"></div></div>
                </section>
              </main>
              <footer><div class="shell foot"><span>Educational prototype. Human verification is required.</span><span>Java 21 · ONNX Runtime Web · YOLOv8 · A*</span></div></footer>
              <script>
                const SIZE=640, LABELS=['Satellite','Debris'];
                let session=null,image=null,detections=[],objectUrl=null;
                const file=document.querySelector('#file'),analyze=document.querySelector('#analyze'),threshold=document.querySelector('#threshold'),thresholdValue=document.querySelector('#thresholdValue'),canvas=document.querySelector('#canvas'),ctx=canvas.getContext('2d'),empty=document.querySelector('#empty'),objects=document.querySelector('#objects'),count=document.querySelector('#count'),modelStatus=document.querySelector('#modelStatus'),modelDot=document.querySelector('#modelDot');

                async function loadModel(){
                  try{
                    ort.env.wasm.numThreads=1;
                    ort.env.wasm.wasmPaths='https://cdn.jsdelivr.net/npm/onnxruntime-web@1.30.0/dist/';
                    session=await ort.InferenceSession.create('/model.onnx',{executionProviders:['wasm']});
                    modelStatus.textContent='Satellite/debris model ready';modelDot.className='dot ready';analyze.disabled=!image;
                  }catch(error){modelStatus.textContent='Model failed to load';modelDot.className='dot error';console.error(error)}
                }

                file.addEventListener('change',()=>{
                  const selected=file.files[0];if(!selected)return;
                  if(objectUrl)URL.revokeObjectURL(objectUrl);objectUrl=URL.createObjectURL(selected);
                  image=new Image();image.onload=()=>{const scale=Math.min(1200/image.naturalWidth,700/image.naturalHeight,1);canvas.width=Math.round(image.naturalWidth*scale);canvas.height=Math.round(image.naturalHeight*scale);canvas.hidden=false;empty.hidden=true;detections=[];draw();renderResults();analyze.disabled=!session};image.src=objectUrl;
                });
                threshold.addEventListener('input',()=>{thresholdValue.value=Number(threshold.value).toFixed(2);draw();renderResults()});
                analyze.addEventListener('click',runModel);

                async function runModel(){
                  if(!session||!image)return;analyze.disabled=true;analyze.textContent='Analyzing…';
                  try{
                    const work=document.createElement('canvas');work.width=SIZE;work.height=SIZE;const wctx=work.getContext('2d',{willReadFrequently:true});wctx.drawImage(image,0,0,SIZE,SIZE);
                    const pixels=wctx.getImageData(0,0,SIZE,SIZE).data,input=new Float32Array(3*SIZE*SIZE),plane=SIZE*SIZE;
                    for(let i=0;i<plane;i++){input[i]=pixels[i*4]/255;input[plane+i]=pixels[i*4+1]/255;input[2*plane+i]=pixels[i*4+2]/255}
                    const tensor=new ort.Tensor('float32',input,[1,3,SIZE,SIZE]);const output=await session.run({[session.inputNames[0]]:tensor});
                    detections=decode(output[session.outputNames[0]]);draw();renderResults();
                  }catch(error){modelStatus.textContent='Inference failed: '+error.message;modelDot.className='dot error';console.error(error)}finally{analyze.disabled=false;analyze.textContent='Run model'}
                }

                function decode(tensor){
                  const data=tensor.data,n=tensor.dims[2],minimum=Number(threshold.value),candidates=[];
                  for(let i=0;i<n;i++){const s0=data[4*n+i],s1=data[5*n+i],classId=s1>s0?1:0,score=Math.max(s0,s1);if(score<minimum)continue;const cx=data[i],cy=data[n+i],w=data[2*n+i],h=data[3*n+i];candidates.push({x1:cx-w/2,y1:cy-h/2,x2:cx+w/2,y2:cy+h/2,score,classId})}
                  candidates.sort((a,b)=>b.score-a.score);const selected=[];
                  for(const box of candidates){if(selected.length>=100)break;if(!selected.some(kept=>kept.classId===box.classId&&iou(kept,box)>.45))selected.push(box)}return selected;
                }
                function iou(a,b){const x1=Math.max(a.x1,b.x1),y1=Math.max(a.y1,b.y1),x2=Math.min(a.x2,b.x2),y2=Math.min(a.y2,b.y2),inter=Math.max(0,x2-x1)*Math.max(0,y2-y1),aa=Math.max(0,a.x2-a.x1)*Math.max(0,a.y2-a.y1),bb=Math.max(0,b.x2-b.x1)*Math.max(0,b.y2-b.y1);return inter/(aa+bb-inter||1)}
                function visible(){const minimum=Number(threshold.value);return detections.filter(d=>d.score>=minimum)}
                function draw(){if(!image)return;ctx.clearRect(0,0,canvas.width,canvas.height);ctx.drawImage(image,0,0,canvas.width,canvas.height);ctx.font='bold 14px system-ui';ctx.lineWidth=3;for(const d of visible()){const x=d.x1/SIZE*canvas.width,y=d.y1/SIZE*canvas.height,w=(d.x2-d.x1)/SIZE*canvas.width,h=(d.y2-d.y1)/SIZE*canvas.height,color=d.classId===1?'#f7b84b':'#70daf2',label=`${LABELS[d.classId]} ${d.score.toFixed(2)}`;ctx.strokeStyle=color;ctx.strokeRect(x,y,w,h);const tw=ctx.measureText(label).width+12;ctx.fillStyle=color;ctx.fillRect(x,Math.max(0,y-24),tw,24);ctx.fillStyle='#071017';ctx.fillText(label,x+6,Math.max(17,y-7))}}
                function renderResults(){const list=visible();count.textContent=list.length;if(!list.length){objects.innerHTML='<div class="empty">No objects above the current threshold.</div>';return}objects.innerHTML=list.map(d=>`<article class="object ${d.classId===1?'debris':''}"><strong><span>${LABELS[d.classId]}</span><span>${d.score.toFixed(2)}</span></strong><small>${d.classId===1?'Potential collision object':'Tracked spacecraft'}</small></article>`).join('')}

                const WIDTH=20,HEIGHT=8,START=[1,4],GOAL=[18,4],initial=['7,2','8,2','8,3','12,4','12,5','13,5'];let obstacles=new Set(initial),path=new Set();const grid=document.querySelector('#orbitGrid'),routeStatus=document.querySelector('#routeStatus'),blockedCount=document.querySelector('#blockedCount'),pathCost=document.querySelector('#pathCost');
                const cells=[];for(let y=0;y<HEIGHT;y++)for(let x=0;x<WIDTH;x++){const button=document.createElement('button');button.className='cell';button.title=`${x},${y}`;button.addEventListener('click',()=>toggle(x,y));grid.appendChild(button);cells.push(button)}
                function key(x,y){return `${x},${y}`}function toggle(x,y){const k=key(x,y);if(k===key(...START)||k===key(...GOAL))return;obstacles.has(k)?obstacles.delete(k):obstacles.add(k);plan()}
                async function plan(){renderGrid();routeStatus.textContent='Java A* calculating…';const body=new URLSearchParams({width:WIDTH,height:HEIGHT,start:START.join(','),goal:GOAL.join(','),obstacles:[...obstacles].join(';')});try{const response=await fetch('/api/plan',{method:'POST',body});if(!response.ok)throw new Error('Planner unavailable');const result=await response.json();path=new Set(result.path.map(p=>key(p[0],p[1])));routeStatus.textContent=result.reachable?`${result.cost} steps · route clear`:'No safe route';pathCost.textContent=result.cost;renderGrid()}catch(error){routeStatus.textContent='Java planner unavailable';console.error(error)}}
                function renderGrid(){blockedCount.textContent=obstacles.size;for(let y=0;y<HEIGHT;y++)for(let x=0;x<WIDTH;x++){const cell=cells[y*WIDTH+x],k=key(x,y);cell.className='cell'+(path.has(k)?' path':'')+(obstacles.has(k)?' obstacle':'')+(k===key(...START)?' start':'')+(k===key(...GOAL)?' goal':'')}}
                document.querySelector('#reset').addEventListener('click',()=>{obstacles=new Set(initial);plan()});renderGrid();plan();loadModel();
              </script>
            </body>
            </html>
            """;
}
