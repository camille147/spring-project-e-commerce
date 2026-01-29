package org.example.springecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LegalController {

    @GetMapping("rgpd/privacy-policy")
    public String privacy() { return "rgpd/privacy-policy"; }

    @GetMapping("rgpd/legal")
    public String legal() { return "rgpd/legal"; }

    @GetMapping("rgpd/terms")
    public String terms() { return "rgpd/terms"; }
}
