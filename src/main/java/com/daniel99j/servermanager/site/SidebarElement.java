package com.daniel99j.servermanager.site;

import java.util.concurrent.atomic.AtomicReference;

public class SidebarElement extends ElementParser {
    public SidebarElement() {
        super("sidebar");
    }

    @Override
    public String parse(String data) {
        return """
                <script>
                    let sidebarOpen = false;
                
                    async function toggleSidebar(event) {
                        sidebarOpen = !sidebarOpen;
                        if(!sidebarOpen) {
                            document.getElementById("sidebar").style.left = "-100px";
                            document.getElementById("dashboard-grid").style = "padding-left: 0px;";
                        }
                        else {
                        document.getElementById("sidebar").style.left = "10px";
                        document.getElementById("dashboard-grid").style = "padding-left: 100px;";
                        }
                    }
                </script>
                <div id="sidebar" style="display: block; position:fixed;  top:10px; left:20px; width:100px; height:100vh;">
                <p>test</p>
                </div>
                <div style="display: block; position:fixed; top:40px; left:20px; scale:3;">
                    <p onclick='toggleSidebar()' style="cursor: pointer;">≡</p>
                </div>
                """;
    }
}
