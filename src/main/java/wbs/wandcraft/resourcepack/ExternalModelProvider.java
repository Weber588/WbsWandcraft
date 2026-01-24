package wbs.wandcraft.resourcepack;

import java.util.List;

public interface ExternalModelProvider extends ItemModelProvider {
    default List<String> getResources() {
        return List.of();
    }
}
