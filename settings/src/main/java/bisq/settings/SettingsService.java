/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.settings;

import bisq.common.application.DevMode;
import bisq.common.application.Service;
import bisq.common.currency.Market;
import bisq.common.locale.LanguageRepository;
import bisq.common.observable.Observable;
import bisq.common.observable.collection.ObservableSet;
import bisq.i18n.Res;
import bisq.persistence.DbSubDirectory;
import bisq.persistence.Persistence;
import bisq.persistence.PersistenceClient;
import bisq.persistence.PersistenceService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SettingsService implements PersistenceClient<SettingsStore>, Service {
    public final static long DEFAULT_MIN_REQUIRED_REPUTATION_SCORE = 30_000;

    @Getter
    private static SettingsService instance;

    @Getter
    private final SettingsStore persistableStore = new SettingsStore();
    @Getter
    private final Persistence<SettingsStore> persistence;
    @Getter
    private final Observable<Boolean> cookieChanged = new Observable<>(false);
    private boolean isInitialized;

    public SettingsService(PersistenceService persistenceService) {
        persistence = persistenceService.getOrCreatePersistence(this, DbSubDirectory.SETTINGS, persistableStore);
        SettingsService.instance = this;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Service
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    public CompletableFuture<Boolean> initialize() {
        log.info("initialize");
        // If used with FxBindings.bindBiDir we need to trigger persist call
        getIsTacAccepted().addObserver(value -> persist());
        getOffersOnly().addObserver(value -> persist());
        getListOffers().addObserver(value -> persist());
        getChatNotificationType().addObserver(value -> persist());
        getUseAnimations().addObserver(value -> persist());
        getPreventStandbyMode().addObserver(value -> persist());
        getMinRequiredReputationScore().addObserver(value -> persist());
        getCloseMyOfferWhenTaken().addObserver(value -> persist());
        getConsumedAlertIds().addObserver(this::persist);
        getSupportedLanguageCodes().addObserver(this::persist);
        getSelectedMarket().addObserver(value -> persist());
        getTradeRulesConfirmed().addObserver(value -> persist());
        getLanguageCode().addObserver(value -> persist());
        getDifficultyAdjustmentFactor().addObserver(value -> persist());
        getIgnoreDiffAdjustmentFromSecManager().addObserver(value -> persist());
        getFavouriteMarkets().addObserver(this::persist);
        getIgnoreMinRequiredReputationScoreFromSecManager().addObserver(value -> persist());
        isInitialized = true;

        if (DevMode.isDevMode() && getMinRequiredReputationScore().get() == DEFAULT_MIN_REQUIRED_REPUTATION_SCORE) {
            getIgnoreMinRequiredReputationScoreFromSecManager().set(true);
            getMinRequiredReputationScore().set(0L);
            log.info("In dev mode we set getMinRequiredReputationScore to 0 if it was the default value of {}",
                    DEFAULT_MIN_REQUIRED_REPUTATION_SCORE);
        }
        return CompletableFuture.completedFuture(true);
    }

    public CompletableFuture<Boolean> shutdown() {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> persist() {
        // We don't want to call persist from the addObserver calls at initialize
        if (isInitialized) {
            return getPersistence().persistAsync(getPersistableStore().getClone())
                    .handle((r, t) -> true);
        } else {
            return CompletableFuture.completedFuture(true);
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onPersistedApplied(SettingsStore persisted) {
        LanguageRepository.setDefaultLanguage(getLanguageCode().get());
        Res.setLanguage(getLanguageCode().get());
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Getters for Observable
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    public Observable<Market> getSelectedMarket() {
        return persistableStore.selectedMarket;
    }

    public Observable<Boolean> getUseAnimations() {
        return persistableStore.useAnimations;
    }

    public Observable<Long> getMinRequiredReputationScore() {
        return persistableStore.minRequiredReputationScore;
    }

    public Observable<Boolean> getOffersOnly() {
        return persistableStore.offersOnly;
    }

    public Observable<Boolean> getListOffers() {
        return persistableStore.listOffers;
    }

    public Observable<Boolean> getTradeRulesConfirmed() {
        return persistableStore.tradeRulesConfirmed;
    }

    public Observable<Boolean> getPreventStandbyMode() {
        return persistableStore.preventStandbyMode;
    }

    public Observable<Boolean> getIgnoreDiffAdjustmentFromSecManager() {
        return persistableStore.ignoreDiffAdjustmentFromSecManager;
    }

    public Observable<Boolean> getIgnoreMinRequiredReputationScoreFromSecManager() {
        return persistableStore.ignoreMinRequiredReputationScoreFromSecManager;
    }

    public Observable<Double> getDifficultyAdjustmentFactor() {
        return persistableStore.difficultyAdjustmentFactor;
    }

    public Observable<ChatNotificationType> getChatNotificationType() {
        return persistableStore.chatNotificationType;
    }

    public Observable<Boolean> getIsTacAccepted() {
        return persistableStore.isTacAccepted;
    }

    public ObservableSet<String> getConsumedAlertIds() {
        return persistableStore.consumedAlertIds;
    }

    public ObservableSet<String> getSupportedLanguageCodes() {
        return persistableStore.supportedLanguageCodes;
    }

    public Observable<Boolean> getCloseMyOfferWhenTaken() {
        return persistableStore.closeMyOfferWhenTaken;
    }

    public Observable<String> getLanguageCode() {
        return persistableStore.languageCode;
    }

    public ObservableSet<Market> getFavouriteMarkets() {
        return persistableStore.favouriteMarkets;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // DontShowAgainMap
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    public Map<String, Boolean> getDontShowAgainMap() {
        return persistableStore.dontShowAgainMap;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Cookie
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    public Cookie getCookie() {
        return persistableStore.cookie;
    }

    public void setCookie(CookieKey key, boolean value) {
        getCookie().putAsBoolean(key, value);
        persist();
        updateCookieChangedFlag();
    }

    public void setCookie(CookieKey key, String subKey, boolean value) {
        key.setSubKey(subKey);
        setCookie(key, value);
    }

    public void setCookie(CookieKey key, double value) {
        getCookie().putAsDouble(key, value);
        persist();
        updateCookieChangedFlag();
    }

    public void setCookie(CookieKey key, String subKey, double value) {
        key.setSubKey(subKey);
        setCookie(key, value);
    }

    public void setCookie(CookieKey key, String value) {
        getCookie().putAsString(key, value);
        persist();
        updateCookieChangedFlag();
    }

    public void removeCookie(CookieKey key) {
        removeCookie(key, null);
    }

    public void removeCookie(CookieKey key, @Nullable String subKey) {
        key.setSubKey(subKey);
        getCookie().remove(key);
        persist();
        updateCookieChangedFlag();
    }

    public void setCookie(CookieKey key, String subKey, String value) {
        key.setSubKey(subKey);
        setCookie(key, value);
    }

    private void updateCookieChangedFlag() {
        cookieChanged.set(!cookieChanged.get());
    }
}
