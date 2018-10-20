package testframeworktest

trait SharedStuff {
	public Map<String, String> getConfigResourceSubstitutes() {
		['global.xml': 'global_test.xml']
	}

	public Map getStartUpProperties() {
		[
			'host.port': 8080
		]
	}
}
