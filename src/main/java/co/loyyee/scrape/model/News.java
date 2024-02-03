package co.loyyee.scrape.model;

import java.time.LocalDateTime;

public record News (String title, String href, Outlet outlet, String website, Category category, LocalDateTime issuedDate) {
}
