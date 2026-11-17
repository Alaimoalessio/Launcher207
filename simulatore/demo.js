"use strict";

// Contratto della demo. La UI legge lo stato e invia comandi a questo adattatore.
const demo = (() => {
  const tracks = [
    { title: "Midnight Drive", artist: "Northline", duration: 228 },
    { title: "Luci di città", artist: "Via Notturna", duration: 196 },
    { title: "Blue Hour", artist: "Marea", duration: 243 },
  ];
  const state = { enabled: true, connected: true, index: 0, playing: false, elapsed: 0, call: "", projection: false };
  return {
    read() {
      return { ...state, track: tracks[state.index] };
    },
    toggleDemo() {
      state.enabled = !state.enabled;
      state.index = 0;
      state.elapsed = 0;
      this.setConnected(state.enabled);
    },
    setConnected(connected) {
      state.connected = state.enabled && connected;
      if (!state.connected) {
        state.playing = false;
        state.call = "";
        state.projection = false;
      }
    },
    togglePlayback() {
      if (state.connected) state.playing = !state.playing;
    },
    skip(step) {
      if (!state.connected) return;
      state.index = (state.index + step + tracks.length) % tracks.length;
      state.elapsed = 0;
    },
    seek(seconds) {
      if (!state.connected || !Number.isFinite(seconds)) return;
      state.elapsed = Math.max(0, Math.min(seconds, tracks[state.index].duration));
    },
    tick(seconds) {
      if (!state.playing) return;
      state.elapsed += seconds;
      if (state.elapsed >= tracks[state.index].duration) this.skip(1);
    },
    dial(number) {
      if (state.connected && /^[0-9*#]{1,20}$/.test(number)) state.call = number;
    },
    hangUp() {
      state.call = "";
    },
    toggleProjection() {
      if (state.connected) state.projection = !state.projection;
    },
  };
})();
