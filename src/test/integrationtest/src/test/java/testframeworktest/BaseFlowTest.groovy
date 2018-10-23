package testframeworktest

import org.junit.Test

import com.avioconsulting.mule.testing.junit.BaseJunitTest

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class BaseFlowTest extends BaseJunitTest implements SharedStuff {	
	@Test
	void first_test() {
		// arrange		
		mockRestHttpCall('Make a request') {
			json {
				whenCalledWith { request ->
					println "got a mock call with ${request}"
					[hello: 123]
				}
			}
		}
		
		// act
		def result = runFlow('testframeworktestFlow') {
			json {
				inputPayload('foobar')
			}
		}
		
		// assert
		assertThat result,
				   is(equalTo([hello: 123]))
	}
}
