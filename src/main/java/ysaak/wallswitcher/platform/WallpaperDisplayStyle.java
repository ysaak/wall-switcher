package ysaak.wallswitcher.platform;

public enum WallpaperDisplayStyle {
	TILE("0"), CENTER("1"), STRETCH("2"), NOCHANGE("3");

	private final String psCode;

	private WallpaperDisplayStyle(final String psCode) {
		this.psCode = psCode;
	}

	public String getPsCode() {
		return psCode;
	}
}
