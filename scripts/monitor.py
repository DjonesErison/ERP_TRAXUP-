#!/usr/bin/env python3
"""Runner-side SSH transport and allowlisted documentation."""
import datetime, json, os, pathlib, re, subprocess, sys
from urllib.request import urlopen, Request
from urllib.error import HTTPError
import tempfile

def sanitize(raw):
    result = {}
    for key in ("frontend", "backend", "postgres"):
        value = raw.get(key)
        result[key] = value if value in ("running", "exited", "restarting", "paused", "created", "dead", "unknown", "missing_or_ambiguous") else "unknown"
    result["postgres_ready"] = raw.get("postgres_ready") is True
    for key in ("frontend_http", "backend_route_http"):
        value = raw.get(key)
        result[key] = value if isinstance(value, str) and re.fullmatch(r"[1-5][0-9]{2}", value) else "unavailable"
    value = raw.get("deployed_commit")
    result["deployed_commit"] = value if isinstance(value, str) and re.fullmatch(r"[0-9a-f]{40}", value) else "unknown"
    return result

def main():
    report = {"timestamp_utc": datetime.datetime.now(datetime.timezone.utc).isoformat(),
              "environment": "homologacao", "ssh": "not_tested",
              "tests": "See PR CI; no business tests run against VPS",
              "deploy": "observation_only", "public_https": "unavailable"}
    try:
        names = ("SSH_HOST", "SSH_USER", "SSH_PORT", "SSH_KEY", "KNOWN_HOSTS")
        if not all(os.environ.get(n) for n in names):
            report["ssh"] = "missing_configuration"
        else:
            host, user, port = (os.environ[n] for n in names[:3])
            if not re.fullmatch(r"[a-zA-Z0-9.:-]+", host) or host.startswith("-") or not re.fullmatch(r"[a-zA-Z0-9_-]+", user) or not port.isdigit() or not 1 <= int(port) <= 65535:
                raise ValueError()
            with tempfile.TemporaryDirectory() as td:
                key, known = pathlib.Path(td)/"key", pathlib.Path(td)/"known_hosts"
                key.write_text(os.environ["SSH_KEY"] + "\n"); key.chmod(0o600)
                known.write_text(os.environ["KNOWN_HOSTS"] + "\n")
                args = ["ssh", "-T", "-F", "/dev/null", "-i", str(key), "-p", port,
                        "-o", "BatchMode=yes", "-o", "IdentitiesOnly=yes",
                        "-o", "StrictHostKeyChecking=yes", "-o", "UserKnownHostsFile="+str(known),
                        "-o", "ConnectTimeout=10", "-o", "LogLevel=QUIET",
                        user+"@"+host, "python3 -"]
                p = subprocess.run(args, input=pathlib.Path(__file__).with_name("monitor_remote.py").read_text(),
                                   capture_output=True, text=True, timeout=120)
                if p.returncode:
                    report["ssh"] = "failed"
                else:
                    data = json.loads(p.stdout)
                    if not isinstance(data, dict): raise ValueError()
                    report["ssh"] = "connected"
                    report["vps"] = sanitize(data)
    except (OSError, ValueError, subprocess.SubprocessError):
        report["ssh"] = "probe_or_transport_error"
    try:
        # Fixed homologacao origin; never follow redirects to other hosts.
        import http.client
        c = http.client.HTTPSConnection("apphomologacao.traxup.com.br", timeout=10)
        c.request("GET", "/")
        report["public_https"] = str(c.getresponse().status)
        c.close()
    except Exception:
        pass
    v = report.get("vps", {})
    report["basic_checks_passed"] = (report["ssh"] == "connected" and
        all(v.get(s) == "running" for s in ("frontend", "backend", "postgres")) and
        v.get("postgres_ready") is True and v.get("frontend_http") == "200" and
        v.get("backend_route_http") == "405" and v.get("deployed_commit") != "unknown" and
        report["public_https"] == "200")
    out = pathlib.Path("monitor-artifact"); out.mkdir(exist_ok=True)
    (out/"report.json").write_text(json.dumps(report, indent=2)+"\n")
    (out/"report.md").write_text("# TRAXUP homologacao — read-only observation\n\n"
        "Basic availability does not prove authenticated flows, migrations, business acceptance or deploy success.\n\n"
        "```json\n"+json.dumps(report, indent=2)+"\n```\n")
    return 0 if report["basic_checks_passed"] else 1

if __name__ == "__main__":
    sys.exit(main())
