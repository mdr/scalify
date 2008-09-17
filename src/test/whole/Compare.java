import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Compare {
	static <T, U> List<U> map(List<T> list, Transformer<T, U> transform) {
		List<U> result = new ArrayList<U>(list.size());
		for (T t : list) {
			result.add(transform.invoke(t));
		}
		return result;
	}

	public static void main(String[] args) {
	        List<Color> colors = map(Arrays.asList(Flavor.values()), new Transformer<Flavor,Color>(){public Color  invoke( Flavor f ){return f.color; }});
	        System.out.println(colors.equals(Arrays.asList(Color.values())));

	        List<Flavor> flavors = map(Arrays.asList(Color.values()), new Transformer<Color,Flavor>(){public Flavor invoke(Color c){return c.flavor;} });
	        System.out.println(flavors.equals(Arrays.asList(Flavor.values())));
	    }
}

interface Transformer<S, T> {
	T invoke(S s);
}

enum Color {
	BROWN(Flavor.CHOCOLATE), RED(Flavor.STRAWBERRY), WHITE(Flavor.VANILLA);
	final Flavor flavor;

	Color(Flavor flavor) {
		this.flavor = flavor;
	}
}

enum Flavor {
	CHOCOLATE(Color.BROWN), STRAWBERRY(Color.RED), VANILLA(Color.WHITE);
	final Color color;

	Flavor(Color color) {
		this.color = color;
	}
}