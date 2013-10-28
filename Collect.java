
public class Collect {
	public static void main(String[] args)
	{
		CollectStreamAPI ct = new CollectStreamAPI();
		ct.entryPoint(args[0]);
		CollectSearchAPI tc = new CollectSearchAPI();
		tc.entryPoint(args[0]);
	}
}
