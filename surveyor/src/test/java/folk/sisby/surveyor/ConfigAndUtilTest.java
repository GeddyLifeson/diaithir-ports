package folk.sisby.surveyor;

import folk.sisby.surveyor.config.NetworkMode;
import folk.sisby.surveyor.config.SystemMode;
import folk.sisby.surveyor.config.lib.Comment;
import folk.sisby.surveyor.config.lib.WrappedConfig;
import folk.sisby.surveyor.util.ArrayUtil;
import folk.sisby.surveyor.util.BitSetUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigAndUtilTest {
	public static class DemoConfig extends WrappedConfig {
		@Comment("a comment")
		public SystemMode mode = SystemMode.DYNAMIC;
		public int count = 5;
		public boolean flag = false;
		public String name = "abc";
	}

	@Test
	void configCreatesFileThenReadsEditedValues(@TempDir Path dir) throws Exception {
		DemoConfig first = WrappedConfig.createToml(dir, "", "demo", DemoConfig.class);
		Path file = dir.resolve("demo.toml");
		assertTrue(Files.exists(file), "defaults are written on first load");
		String text = Files.readString(file);
		assertTrue(text.contains("# a comment"));
		assertTrue(text.contains("mode = \"DYNAMIC\""));
		Files.writeString(file, text.replace("count = 5", "count = 9").replace("flag = false", "flag = true").replace("\"DYNAMIC\"", "\"ENABLED\""));
		DemoConfig second = WrappedConfig.createToml(dir, "", "demo", DemoConfig.class);
		assertEquals(9, second.count);
		assertTrue(second.flag);
		assertEquals(SystemMode.ENABLED, second.mode);
		assertEquals("abc", second.name);
		assertEquals(5, first.count);
	}

	@Test
	void badValueFallsBackToDefaultInsteadOfCrashing(@TempDir Path dir) throws Exception {
		Files.writeString(dir.resolve("demo.toml"), "count = \"not a number\"\nmode = \"NOPE\"\n");
		DemoConfig c = WrappedConfig.createToml(dir, "", "demo", DemoConfig.class);
		assertEquals(5, c.count);
		assertEquals(SystemMode.DYNAMIC, c.mode);
	}

	@Test
	void modeOrderingAndSharing() {
		assertTrue(SystemMode.ENABLED.atLeast(SystemMode.DYNAMIC));
		assertFalse(SystemMode.FROZEN.atLeast(SystemMode.DYNAMIC));
		assertTrue(NetworkMode.SERVER.atLeast(NetworkMode.GROUP));
		assertTrue(NetworkMode.GROUP.atLeast(NetworkMode.SOLO));
		assertFalse(NetworkMode.NONE.atLeast(NetworkMode.SOLO));
	}

	@Test
	void singleValueArrays() {
		assertArrayEquals(new int[]{7, 7, 7}, ArrayUtil.ofSingle(7, 3));
		assertTrue(ArrayUtil.isSingle(new int[]{4, 4, 4, 4}));
		assertFalse(ArrayUtil.isSingle(new int[]{4, 4, 5, 4}));
	}

	@Test
	void bitSetHalvesKeepEveryBit() {
		BitSet all = new BitSet(1024);
		for (int i = 0; i < 1024; i += 3) all.set(i);
		BitSet merged = new BitSet(1024);
		BitSetUtil.half(all).forEach(merged::or);
		assertEquals(all, merged);
	}
}
