import org.apache.ignite.scalar._
import scalar._
import com.dfine.moco._

ignite$.compute().run( () => {
	   
			import com.dfine.moco._
			MoCoLoader.loadMoco()
			var sessID = MoCoSessionWrapper.createSession()
			
			println(MoCoWrapper.Release(sessID))
			

			MoCoSessionWrapper.deleteSession(sessID) });

