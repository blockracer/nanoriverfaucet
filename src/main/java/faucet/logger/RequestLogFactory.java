package faucet;
//jetty
import org.eclipse.jetty.server.RequestLog.Writer;
import org.eclipse.jetty.server.Slf4jRequestLog;
//apache log4j
import org.apache.log4j.Logger;

import java.io.IOException;

public class RequestLogFactory {

	private Logger logger;

    	public RequestLogFactory(Logger logger) {
        	this.logger = logger;
    	}

    	Slf4jRequestLog create() {
        	return new Slf4jRequestLog() {
            		@Override
            		protected boolean isEnabled() {
                		return true;
            		}

            		@Override
            		public void write(String s) throws IOException {
                		logger.info(s);
            		}
        	};
    	}
}
