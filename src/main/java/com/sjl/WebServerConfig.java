package com.sjl;

public interface WebServerConfig 
{
	public String getServerName();

	public int getPort();

	public String getHostInterface();

	public int getMinThreads();

	public int getMaxThreads();

	public String getAccessLogDirectory();	
	
	public class Factory {
		
		public static WebServerConfig newDevelopmentConfig(String aName, int aPort, String anInterface) {
			return new Development(aName, aPort, anInterface);
		}
		
		public static WebServerConfig newProductionConfig(String aName, int aPort, String anInterface, int aMinThreads, int aMaxThreads) {
			return new Production(aName, aPort, anInterface, aMinThreads, aMaxThreads);
		}
	
		static abstract class AbstractWebServerConfig implements WebServerConfig
		{
			private String name;
			private int port;
			private String intf;
			private int minThreads;
			private int maxThreads;
			
			private AbstractWebServerConfig(String aName, int aPort, String anInterface, int aMinThreads, int aMaxThreads)
			{
				name = aName;
				port = aPort;
				intf = anInterface;
				minThreads = aMinThreads;
				maxThreads = aMaxThreads;
			}
			
			@Override
			public String getServerName()
			{
				return name;
			}

			@Override
			public int getPort()
			{
				return port;
			}

			@Override
			public String getHostInterface()
			{
				return intf;
			}

			@Override
			public int getMinThreads()
			{
				return minThreads;
			}

			@Override
			public int getMaxThreads()
			{
				return maxThreads;
			}

			@Override
			public String getAccessLogDirectory()
			{
				return String.format("./var/logs/$s/", name);
			}
		}
		
		public static final class Development extends AbstractWebServerConfig
		{
			public Development(String aName, int aPort, String anInterface)
			{
				super(aName, aPort, anInterface, 5, 15);
			}
		}
		
		public static final class Production extends AbstractWebServerConfig
		{		
			public Production(String aName, int aPort, String anInterface, int aMinThreads, int aMaxThreads)
			{
				super(aName, aPort, anInterface, aMinThreads, aMaxThreads);
			}
		}		
	}		
}
