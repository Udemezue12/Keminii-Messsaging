(() => {
    "use strict";

    /* ----------------------------------------------------------------------
       Copy-to-clipboard buttons
       Handles both data-copy="literal text" and data-copy-target="elementId"
    ---------------------------------------------------------------------- */
    function initCopyButtons() {
        const buttons = document.querySelectorAll("[data-copy], [data-copy-target]");

        buttons.forEach((btn) => {
            btn.addEventListener("click", async () => {
                let text = btn.getAttribute("data-copy");

                if (!text) {
                    const targetId = btn.getAttribute("data-copy-target");
                    const target = targetId ? document.getElementById(targetId) : null;
                    text = target ? target.textContent.trim() : "";
                }

                if (!text) return;

                try {
                    await navigator.clipboard.writeText(text);
                    flashCopied(btn);
                } catch (err) {
                    // Clipboard API unavailable (older browser, insecure context) — fail quietly.
                    flashCopied(btn, "Copy failed");
                }
            });
        });
    }

    function flashCopied(btn, label) {
        const original = btn.textContent;
        btn.textContent = label || "Copied";
        btn.disabled = true;
        window.setTimeout(() => {
            btn.textContent = original;
            btn.disabled = false;
        }, 1400);
    }

    /* ----------------------------------------------------------------------
       Hero frame inspector — cycles through plausible frame states
       to suggest a live, continuously-updating connection.
    ---------------------------------------------------------------------- */
    function initFrameInspector() {
        const opEl = document.querySelector('[data-cycle="op"]');
        const maskEl = document.querySelector('[data-cycle="mask"]');
        const payloadEl = document.querySelector('[data-cycle="payload"]');
        const stateEl = document.querySelector('[data-cycle="state"]');

        if (!opEl || !maskEl || !payloadEl || !stateEl) return;

        const ops = ["0x1 TEXT", "0x2 BINARY", "0x9 PING", "0xA PONG"];
        const states = ["ENCRYPTING", "IN TRANSIT", "DECRYPTING", "DELIVERED"];

        function randomHex(len) {
            const chars = "0123456789abcdef";
            let out = "";
            for (let i = 0; i < len; i++) {
                out += chars[Math.floor(Math.random() * chars.length)];
            }
            return out;
        }

        function tick() {
            opEl.textContent = ops[Math.floor(Math.random() * ops.length)];
            maskEl.textContent = Math.random() > 0.15 ? "true" : "false";
            payloadEl.textContent = `${randomHex(4)}\u2026${randomHex(4)}`;
            stateEl.textContent = states[Math.floor(Math.random() * states.length)];
        }

        tick();
        window.setInterval(tick, 1600);
    }

    /* ----------------------------------------------------------------------
       Simulated live console
       Purely illustrative: shows what a real exchange over
       ws://localhost:8989 looks like once a client connects.
    ---------------------------------------------------------------------- */
    function initConsoleDemo() {
        const log = document.getElementById("console-log");
        const status = document.getElementById("console-status");
        if (!log || !status) return;

        const script = [
            { tag: "connect", text: "opening ws://localhost:8989 \u2026" },
            { tag: "sent", text: 'client_a \u2192 {"type":"message","body":"hey, you there?"}' },
            { tag: "recv", text: "server \u2190 frame encrypted \u00b7 47 bytes \u00b7 sent" },
            { tag: "recv", text: 'client_b \u2192 {"type":"message","body":"reading you loud and clear"}' },
            { tag: "sent", text: "server \u2192 frame decrypted \u00b7 delivered in 12ms" },
            { tag: "sent", text: 'client_a \u2192 {"type":"typing","state":true}' },
            { tag: "recv", text: "server \u2190 heartbeat \u00b7 connection healthy" },
        ];

        let index = 0;
        let timer = null;

        function tagClass(tag) {
            if (tag === "sent") return "tag-sent";
            if (tag === "recv") return "tag-recv";
            return "";
        }

        function appendLine(entry) {
            const line = document.createElement("div");
            line.className = `console-line ${tagClass(entry.tag)}`.trim();
            line.innerHTML =
                `<span class="line-tag">[${entry.tag}]</span>` +
                `<span class="line-payload"></span>`;
            line.querySelector(".line-payload").textContent = entry.text;
            log.appendChild(line);
            log.scrollTop = log.scrollHeight;

            // Cap visible history so the panel doesn't grow unbounded.
            while (log.children.length > 8) {
                log.removeChild(log.firstChild);
            }
        }

        function playStep() {
            if (index === 0) {
                status.textContent = "connecting\u2026";
                status.classList.remove("is-live");
            }

            appendLine(script[index]);

            if (index === 0) {
                window.setTimeout(() => {
                    status.textContent = "live";
                    status.classList.add("is-live");
                }, 500);
            }

            index = (index + 1) % script.length;
            timer = window.setTimeout(playStep, index === 0 ? 2600 : 1500);
        }

        // Only run the animation while the console is visible on screen.
        const observer = new IntersectionObserver(
            (entries) => {
                entries.forEach((entry) => {
                    if (entry.isIntersecting && !timer) {
                        playStep();
                    } else if (!entry.isIntersecting && timer) {
                        window.clearTimeout(timer);
                        timer = null;
                    }
                });
            },
            { threshold: 0.25 }
        );

        observer.observe(log.closest(".console"));
    }

    document.addEventListener("DOMContentLoaded", () => {
        initCopyButtons();
        initFrameInspector();
        initConsoleDemo();
    });
})();