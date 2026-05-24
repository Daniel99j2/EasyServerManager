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
                        }
                        else {
                        document.getElementById("sidebar").style.left = "10px";
                        }
                    }
                </script>
                <div class="sidebar" id="sidebar" style="display: flex; position:fixed;  top:10px; left:20px; width:100px; height:calc(100vh - 20px); background-color: #29292af0; border-radius: 5px; left: -100px;">
                    <h1>test</p>
                </div>
                <div class="toggle-sidebar" onclick="toggleSidebar()">≡</div>
                """;
    }
}
