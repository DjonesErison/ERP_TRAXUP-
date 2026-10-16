import unittest
from unittest.mock import patch
import monitor
import monitor_remote

class MonitorTests(unittest.TestCase):
    def test_secrets_and_unexpected_fields_are_discarded(self):
        raw = {"password": "DO_NOT_PUBLISH", "frontend": "DO_NOT_PUBLISH",
               "backend_route_http": "200\nDO_NOT_PUBLISH", "deployed_commit": "DO_NOT_PUBLISH"}
        self.assertNotIn("DO_NOT_PUBLISH", str(monitor.sanitize(raw)))
        self.assertNotIn("password", monitor.sanitize(raw))

    def test_ambiguous_containers_are_never_executed(self):
        calls = []
        def fake(args):
            calls.append(args)
            if args[:2] == ["docker", "ps"]:
                return 0, "a"*12+"\n"+"b"*12
            return 1, ""
        with patch.object(monitor_remote, "run", side_effect=fake):
            result = monitor_remote.probe()
        self.assertEqual(result["postgres"], "missing_or_ambiguous")
        self.assertFalse(any("exec" in a or "inspect" in a for a in calls))
        for args in calls:
            if args[:2] == ["docker", "ps"]:
                self.assertIn("label=com.docker.compose.project=traxup-homologacao", args)

    def test_valid_evidence_survives(self):
        result = monitor.sanitize({"frontend": "running", "postgres_ready": True,
             "frontend_http": "200", "backend_route_http": "405", "deployed_commit": "a"*40})
        self.assertEqual(result["deployed_commit"], "a"*40)
        self.assertTrue(result["postgres_ready"])

if __name__ == "__main__":
    unittest.main()
