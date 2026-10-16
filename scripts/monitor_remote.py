#!/usr/bin/env python3
"""Read-only probes. Never emit raw command output, environment, logs or DB rows."""
import json, re, subprocess

PROJECT = "traxup-homologacao"

def run(args):
    try:
        p = subprocess.run(args, capture_output=True, text=True, timeout=15)
        return p.returncode, p.stdout[:4096]
    except (OSError, subprocess.SubprocessError):
        return 127, ""

def http(path, body=False):
    args = ["curl", "--silent", "--max-time", "8", "--noproxy", "*",
            "--proto", "=http", "--max-filesize", "4096"]
    args += [] if body else ["--output", "/dev/null", "--write-out", "%{http_code}"]
    return run(args + ["http://127.0.0.1:8082" + path])

def probe():
    report = {}
    for service in ("frontend", "backend", "postgres"):
        rc, output = run(["docker", "ps", "-aq", "--filter",
                         "label=com.docker.compose.project=" + PROJECT,
                         "--filter", "label=com.docker.compose.service=" + service])
        ids = output.split()
        if rc or len(ids) != 1 or not re.fullmatch(r"[0-9a-f]{12,64}", ids[0]):
            report[service] = "missing_or_ambiguous"
            continue
        cid = ids[0]
        rc, state = run(["docker", "inspect", "--format", "{{.State.Status}}", cid])
        report[service] = state.strip() if not rc and state.strip() in (
            "running", "exited", "restarting", "paused", "created", "dead") else "unknown"
        if service == "postgres":
            rc, _ = run(["docker", "exec", cid, "pg_isready", "-q",
                         "-U", "traxup_homologacao", "-d", "traxup_homologacao"])
            report["postgres_ready"] = rc == 0
    rc, code = http("/")
    report["frontend_http"] = code if not rc and re.fullmatch(r"[1-5][0-9]{2}", code) else "unavailable"
    # GET is intentionally side-effect free: 405 confirms the known POST-only route.
    rc, code = http("/api/v1/auth/login")
    report["backend_route_http"] = code if not rc and re.fullmatch(r"[1-5][0-9]{2}", code) else "unavailable"
    rc, metadata = http("/build-info.json", True)
    try:
        sha = json.loads(metadata).get("commit", "") if not rc else ""
    except (ValueError, AttributeError):
        sha = ""
    report["deployed_commit"] = sha if isinstance(sha, str) and re.fullmatch(r"[0-9a-f]{40}", sha) else "unknown"
    return report

if __name__ == "__main__":
    print(json.dumps(probe()))
