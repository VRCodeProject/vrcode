#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunknown-pragmas"
#pragma ide diagnostic ignored "cppcoreguidelines-pro-type-static-cast-downcast"
#include <lorie-compositor.hpp>
#include <lorie-client.hpp>
#include <cstdio>
#include <cassert>

LorieClient::LorieClient(struct wl_client* client, LorieCompositor &compositor) : wl_listener(),
	compositor(compositor), client(client) {
	notify = &destroyed;
	wl_client_add_destroy_listener(client, this);
	LOGI("Client created");
}

void LorieClient::destroyed(struct wl_listener *listener, void *data) {
	auto* c = static_cast<LorieClient*>(listener);
	if (c == nullptr) return;

	if (c->compositor.toplevel && c->compositor.toplevel->client == *c)
		c->compositor.set_toplevel(nullptr);
	
	if (c->compositor.cursor && c->compositor.cursor->client == *c)
		c->compositor.set_cursor(nullptr, 0, 0);
	
	LOGI("Client destroyed");
	delete c;
}

LorieClient* LorieClient::get(struct wl_client* client) {
	if (client == nullptr) return nullptr;
	return static_cast<LorieClient*>(wl_client_get_destroy_listener(client, &destroyed));
}

LorieClient& LorieClient::get() {
	return *this;
}

#pragma clang diagnostic pop