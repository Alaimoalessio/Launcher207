"use strict";

const paths = {
  home: '<path d="m3 11 9-8 9 8v9h-6v-6H9v6H3z"/>',
  auto: '<path d="m4 19 7-15 9 15-8-4z"/>',
  music: '<path d="M9 18V5l11-2v13M9 18a3 2 0 1 1-6 0 3 2 0 0 1 6 0Zm11-2a3 2 0 1 1-6 0 3 2 0 0 1 6 0Z"/>',
  phone: '<path d="m7 3 3 5-2 2a14 14 0 0 0 6 6l2-2 5 3-1 4C11 22 2 13 3 4z"/>',
  settings: '<circle cx="12" cy="12" r="3"/><path d="m10 2-.6 3.2L7 6.6 4 5.5 2 9l2.5 2v2L2 15l2 3.5 3-1.1 2.4 1.4L10 22h4l.6-3.2 2.4-1.4 3 1.1 2-3.5-2.5-2v-2L22 9l-2-3.5-3 1.1-2.4-1.4L14 2z"/>',
  bluetooth: '<path d="m6 6 12 12-6 4V2l6 4L6 18"/>',
  back: '<path d="m11 5-7 7 7 7M4 12h16"/>',
  previous: '<path d="M5 5v14m14-14L8 12l11 7z"/>',
  next: '<path d="M19 5v14M5 5l11 7-11 7z"/>',
  play: '<path fill="currentColor" d="m8 5 12 7-12 7z"/>',
  pause: '<path d="M9 5v14m6-14v14"/>',
  erase: '<path d="M8 5h13v14H8l-6-7zM11 9l6 6m0-6-6 6"/>',
};
const icon = (name) => `<svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">${paths[name]}</svg>`;
const escapeHtml = (value) => String(value).replace(/[&<>"']/g, (character) =>
  ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[character]);
const screen = document.querySelector("#screen");
const titles = { music: "Musica Bluetooth", phone: "Telefono", settings: "Impostazioni", auto: "Android Auto" };
const disabled = (condition) => condition ? "disabled" : "";
let page = "home";
let number = "";

function fitDisplay() {
  const scale = Math.min(1, innerWidth / 1024, innerHeight / 600);
  const frame = document.querySelector("#frame");
  frame.style.width = `${1024 * scale}px`;
  frame.style.height = `${600 * scale}px`;
  document.querySelector("#display").style.transform = `scale(${scale})`;
}

function updateClock() {
  const clock = document.querySelector("#clock");
  if (!clock) return;
  const now = new Date();
  const time = now.toLocaleTimeString("it-IT", { hour: "2-digit", minute: "2-digit" });
  if (clock.textContent === time) return;
  clock.textContent = time;
  document.querySelector(".clock").style.setProperty("--minute-angle", `${now.getMinutes() * 6}deg`);
  clock.dateTime = now.toISOString();
  document.querySelector("#date").textContent = now.toLocaleDateString("it-IT", { weekday: "long", day: "numeric", month: "long" });
}

const dockItems = [
  ["home", "Home", "home"],
  ["auto", "Android Auto", "auto"],
  ["music", "Musica", "music"],
  ["phone", "Telefono", "phone"],
];

document.querySelector("#dock").innerHTML = dockItems.map(([target, label, symbol]) =>
  `<button class="dock-item" data-go="${target}">${icon(symbol)}<span>${label}</span></button>`
).join("");
document.querySelector("#settings-link").innerHTML = icon("settings");

function transport(state) {
  return `<div class="transport">
    ${control("prev", "Brano precedente", "previous", !state.connected)}
    ${control("play", state.playing ? "Pausa" : "Riproduci", state.playing ? "pause" : "play", !state.connected, true)}
    ${control("next", "Brano successivo", "next", !state.connected)}
  </div>`;
}

function home(state) {
  return `<div class="home">
    <div class="clock" tabindex="-1" role="group" aria-label="Ora e data">
      <span class="clock-ticks" aria-hidden="true"></span>
      <span class="minute-marker" aria-hidden="true"></span>
      <time id="clock"></time><p id="date"></p>
    </div>
    <div class="home-media">
      <p class="source">${icon("music")} Musica Bluetooth</p>
      <div class="media-info">
        <h1>${state.connected ? escapeHtml(state.track.title) : "Sorgente scollegata"}</h1>
        <p class="subtle">${state.connected ? escapeHtml(state.track.artist) : "Connetti il telefono per riprodurre musica."}</p>
      </div>
      ${state.connected ? transport(state) : '<button class="action" data-go="settings">Apri impostazioni</button>'}
    </div>
  </div>`;
}

const formatTime = (seconds) => `${Math.floor(seconds / 60)}:${String(Math.floor(seconds % 60)).padStart(2, "0")}`;
const control = (action, label, symbol, locked, primary = false) =>
  `<button class="icon${primary ? " primary" : ""}" data-action="${action}" aria-label="${label}" ${disabled(locked)}>${icon(symbol)}</button>`;

function music(state) {
  const { track, connected } = state;
  return `<div class="music">
    <div class="track">${icon("music")}<div>
      <h2>${connected ? escapeHtml(track.title) : "Nessun brano"}</h2>
      <p class="subtle">${connected ? escapeHtml(track.artist) : "Sorgente Bluetooth non disponibile"}</p>
    </div></div>
    <div class="player">
      <div class="timeline">
        <input id="seek" type="range" min="0" max="${connected ? track.duration : 1}" value="${connected ? state.elapsed : 0}" aria-label="Posizione nel brano" ${disabled(!connected)}>
        <div class="times"><span id="elapsed"></span><span>${formatTime(connected ? track.duration : 0)}</span></div>
      </div>
      ${transport(state)}
    </div>
  </div>`;
}

function phone(state) {
  const locked = !state.connected || Boolean(state.call);
  return `<div class="phone">
    <div class="dial">
      <div>
        <p id="call-status" class="subtle" role="status">${state.call ? "Chiamata simulata in corso" : state.connected ? "Tastierino numerico" : "Telefono non disponibile"}</p>
        <p class="number" aria-live="polite">${escapeHtml(state.call || number || "—")}</p>
      </div>
      <div class="dial-actions">
        <button class="${state.call ? "end-call" : "primary"}" data-action="call" ${disabled(!state.connected || (!number && !state.call))}>${state.call ? "Termina chiamata" : "Simula chiamata"}</button>
        ${control("erase", "Cancella ultima cifra", "erase", locked || !number)}
      </div>
    </div>
    <div class="keypad" role="group" aria-label="Tastierino">
      ${[..."123456789*0#"].map((digit) => `<button data-action="digit" data-digit="${digit}" ${disabled(locked || number.length >= 20)}>${digit}</button>`).join("")}
    </div>
  </div>`;
}

function settings(state) {
  return `<div class="settings">
    <div class="setting"><div><strong>Modalità demo</strong><p class="subtle">Brani, chiamate e connessione simulati</p></div>
      <button data-action="demo" aria-pressed="${state.enabled}">${state.enabled ? "Disattiva demo" : "Attiva demo"}</button>
    </div>
    <div class="setting"><div><strong>Bluetooth</strong><p class="subtle">${state.connected ? "Telefono demo connesso" : "Nessun telefono connesso"}</p></div>
      <button data-action="connect" ${disabled(!state.enabled)}>${state.connected ? "Disconnetti" : "Connetti telefono demo"}</button>
    </div>
  </div>`;
}

function projection(state) {
  return `<div class="auto">${icon("auto")}
    <h2>${state.projection ? "Sessione demo attiva" : "Proiezione Android Auto"}</h2>
    <p class="subtle">${state.connected ? "Anteprima dello stato di connessione alla proiezione." : "Connetti il telefono demo dalle impostazioni."}</p>
    <button class="action primary" ${state.connected ? 'data-action="projection"' : 'data-go="settings"'}>${state.projection ? "Chiudi demo" : state.connected ? "Avvia demo" : "Apri impostazioni"}</button>
  </div>`;
}

const pages = { home, music, phone, settings, auto: projection };

function updateTimeline(state) {
  const seek = document.querySelector("#seek");
  if (!seek) return;
  const elapsed = state.connected ? Math.floor(state.elapsed) : 0;
  seek.value = elapsed;
  seek.setAttribute("aria-valuetext", formatTime(elapsed));
  document.querySelector("#elapsed").textContent = formatTime(elapsed);
}

function render({ navigate = false, restore = false } = {}) {
  const active = document.activeElement;
  const state = demo.read();
  for (const button of document.querySelectorAll("#dock button, #settings-link")) {
    if (button.dataset.go === page) button.setAttribute("aria-current", "page");
    else button.removeAttribute("aria-current");
  }
  const auto = document.querySelector('#dock [data-go="auto"]');
  auto.classList.toggle("session-active", state.projection);
  auto.classList.toggle("unavailable", !state.connected);
  auto.setAttribute("aria-label", state.projection ? "Android Auto, sessione demo attiva" :
    state.connected ? "Android Auto" : "Android Auto, proiezione non disponibile");
  const status = document.querySelector("#status");
  status.classList.toggle("connected", state.connected);
  const connection = state.connected ? "Bluetooth demo connesso" : "Bluetooth disconnesso";
  if (status.textContent !== connection) status.innerHTML = icon("bluetooth") + connection;
  document.querySelector("#mode").textContent = state.enabled ? "Demo attiva" : "Demo disattivata";
  const heading = page === "home" ? "" : `<div class="page-head">
    <button class="back" data-go="home" aria-label="Torna alla Home">${icon("back")}</button>
    <h1 tabindex="-1">${titles[page]}</h1>
  </div>`;
  screen.innerHTML = heading + pages[page](state);
  updateClock();
  updateTimeline(state);
  if (navigate) {
    (page === "home" ? screen.querySelector(".clock") : screen.querySelector("h1")).focus();
  } else if (restore && active?.dataset.action) {
    const replacement = [...screen.querySelectorAll("[data-action]")].find((button) =>
      button.dataset.action === active.dataset.action && button.dataset.digit === active.dataset.digit);
    (replacement?.disabled ? screen.querySelector("h1") : replacement)?.focus();
  }
}

document.addEventListener("click", (event) => {
  const button = event.target.closest("button");
  if (!button || button.disabled) return;
  const { go, action, digit } = button.dataset;
  if (go) {
    if (!Object.hasOwn(pages, go)) return;
    page = go;
    render({ navigate: true });
    return;
  }
  const state = demo.read();
  switch (action) {
    case "prev": demo.skip(-1); break;
    case "next": demo.skip(1); break;
    case "play": demo.togglePlayback(); break;
    case "demo": demo.toggleDemo(); number = ""; break;
    case "connect": demo.setConnected(!state.connected); break;
    case "projection": demo.toggleProjection(); break;
    case "digit": number += digit; break;
    case "erase": number = number.slice(0, -1); break;
    case "call":
      if (state.call) demo.hangUp();
      else demo.dial(number);
      break;
    default: return;
  }
  render({ restore: true });
});

screen.addEventListener("input", (event) => {
  if (event.target.id !== "seek") return;
  demo.seek(event.target.valueAsNumber);
  updateTimeline(demo.read());
});

document.addEventListener("keydown", (event) => {
  if (event.key !== "Escape" || page === "home") return;
  page = "home";
  render({ navigate: true });
});

let lastTick = performance.now();
setInterval(() => {
  const now = performance.now();
  const before = demo.read();
  demo.tick((now - lastTick) / 1000);
  lastTick = now;
  const after = demo.read();
  if (before.index !== after.index && (page === "music" || page === "home")) render({ restore: true });
  else if (page === "music") updateTimeline(after);
  else if (page === "home") updateClock();
}, 250);

window.addEventListener("resize", fitDisplay);
fitDisplay();
render();
