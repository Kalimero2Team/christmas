package de.beyondblocks.plugins.christmas.util.board;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.sql.SQLException;

public interface PlaceholderProvider {

    TagResolver getPlaceholder() throws SQLException;

}
