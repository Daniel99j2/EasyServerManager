package com.daniel99j.servermanager.site;

public class SiteTitleElement extends ElementParser {
    public SiteTitleElement() {
        super("sitetitle");
    }

    @Override
    public String parse(String data) {
        return """
                <script type="text/javascript">
                    function openHomePage() {
                        window.location.href = "%base%"
                    }
                </script>
                <p style="color: lime; cursor: pointer;" onclick='openHomePage()'>Easy Server Manager</p>
                """;
    }
}
