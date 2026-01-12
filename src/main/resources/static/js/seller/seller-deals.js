// 판매자 거래 관리 페이지 스크립트

(() => {
    const API_URL = '/api/seller/deals';

    /** 탭 이름 상수 */
    const TABS = ['all', 'pending', 'ongoing', 'completed', 'cancelled'];

    /** 상태별 탭 매핑 */
    const STATUS_TO_TAB = {
        PENDING_CONFIRMATION: 'pending',   // 거래 대기
        CONFIRMED: 'ongoing',             // 진행 중
        COMPLETED: 'completed',           // 완료
        TERMINATED: 'cancelled',          // 취소/만료
        EXPIRED: 'cancelled'              // 취소/만료
    };

    /** 상태 표시용 설정 */
    const STATUS_CONFIG = {
        PENDING_CONFIRMATION: {
            label: '거래 대기',
            badgeClass: 'bg-yellow-100 text-yellow-800',
        },
        CONFIRMED: {
            label: '진행 중',
            badgeClass: 'bg-blue-100 text-blue-800',
        },
        COMPLETED: {
            label: '완료',
            badgeClass: 'bg-green-100 text-green-800',
        },
        TERMINATED: {
            label: '취소됨',
            badgeClass: 'bg-red-100 text-red-800',
        },
        EXPIRED: {
            label: '만료됨',
            badgeClass: 'bg-gray-100 text-gray-800',
        },
        DEFAULT: {
            label: '알 수 없음',
            badgeClass: 'bg-gray-100 text-gray-800',
        }
    };

    let allDeals = [];
    let currentTab = 'all';

    /** 공통 fetch 유틸 */
    async function fetchJson(url, options = {}) {
        const resp = await fetch(url, {
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest',
            },
            credentials: 'include',
            ...options,
        });

        if (!resp.ok) {
            let message = `요청에 실패했습니다. (HTTP ${resp.status})`;
            try {
                const data = await resp.json();
                if (data && data.message) {
                    message = data.message;
                }
            } catch (e) {
                // noop
            }
            throw new Error(message);
        }

        return resp.json();
    }

    /** 날짜 포맷팅 */
    function formatDate(value) {
        if (!value) return '-';
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) return '-';

        return new Intl.DateTimeFormat('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        }).format(date);
    }

    /** 금액 포맷팅 */
    function formatPrice(value) {
        if (value == null) return '-';
        const num = Number(value);
        if (Number.isNaN(num)) return '-';
        return `${num.toLocaleString('ko-KR')}원`;
    }

    /** 상태 텍스트 + 뱃지 클래스 */
    function getStatusMeta(status) {
        return STATUS_CONFIG[status] || STATUS_CONFIG.DEFAULT;
    }

    /** 현재 탭에 맞게 거래 필터링 */
    function filterDealsByTab(tabName) {
        if (tabName === 'all') return allDeals;

        return allDeals.filter((deal) => {
            const mappedTab = STATUS_TO_TAB[deal.status];
            return mappedTab === tabName;
        });
    }

    /** 탭 카운트 업데이트 */
    function updateTabCounts() {
        const counts = {
            all: allDeals.length,
            pending: 0,
            ongoing: 0,
            completed: 0,
            cancelled: 0,
        };

        allDeals.forEach((deal) => {
            const tab = STATUS_TO_TAB[deal.status];
            if (tab && counts[tab] != null) {
                counts[tab] += 1;
            }
        });

        const setText = (id, value) => {
            const el = document.getElementById(id);
            if (el) el.textContent = value;
        };

        setText('all-count', counts.all);
        setText('pending-count', counts.pending);
        setText('ongoing-count', counts.ongoing);
        setText('completed-count', counts.completed);
        setText('cancelled-count', counts.cancelled);
    }

    /** 탭 콘텐츠 안에서 실제 리스트를 렌더링할 컨테이너 찾기
     *  (HTML 구조가 약간 바뀌어도 최대한 잘 찾도록 여유 있게 작성)
     */
    function getListContainer(tabName) {
        const root = document.getElementById(`tab-${tabName}`);
        if (!root) return null;

        return (
            root.querySelector('.space-y-4') ||
            root.querySelector('.space-y-6') ||
            root
        );
    }

    /** 거래 카드 하나 렌더링 */
    function createDealCard(deal) {
        const statusMeta = getStatusMeta(deal.status);

        // 서비스에서 아직 경매 제목/이미지를 내려주지 않으니,
        // 일단 기본 텍스트와 placeholder 이미지 사용
        const title = deal.auctionTitle || `거래 #${deal.dealId}`;
        const thumbnailUrl = deal.thumbnailUrl || '/img/placeholder-image.png';

        const createdAtText = formatDate(deal.createdAt);
        const priceText = formatPrice(deal.finalPrice);

        return `
            <div class="relative flex gap-4 p-4 bg-white border border-gray-100 rounded-xl shadow-sm hover:shadow-md transition-shadow">
                <!-- 이미지 -->
                <div class="flex-shrink-0">
                    <div class="w-24 h-24 rounded-lg overflow-hidden bg-gray-100">
                        <img 
                            src="${thumbnailUrl}" 
                            alt="상품 이미지" 
                            class="w-full h-full object-cover"
                            onerror="this.src='/img/placeholder-image.png';"
                        />
                    </div>
                </div>

                <!-- 본문 -->
                <div class="flex-1 flex flex-col justify-between min-w-0">
                    <div>
                        <div class="flex items-center gap-2">
                            <h2 class="text-base font-semibold text-gray-900 truncate">
                                ${title}
                            </h2>
                            <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${statusMeta.badgeClass}">
                                ${statusMeta.label}
                            </span>
                        </div>
                        <p class="mt-1 text-xs text-gray-500">
                            생성일시 · ${createdAtText}
                        </p>
                    </div>

                    <div class="mt-3 flex items-center justify-between">
                        <div class="text-sm">
                            <span class="text-gray-500">거래 금액</span>
                            <span class="ml-2 font-semibold text-gray-900">${priceText}</span>
                        </div>

                        <div class="flex gap-2">
                            <button
                                type="button"
                                class="seller-deal-detail-btn inline-flex items-center px-3 py-1.5 rounded-lg border border-gray-200 text-xs font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                                data-deal-id="${deal.dealId}">
                                상세보기
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    /** 탭별 리스트 렌더링 */
    function renderTab(tabName) {
        const container = getListContainer(tabName);
        if (!container) return;

        const deals = filterDealsByTab(tabName);

        if (!deals.length) {
            let message = '';
            switch (tabName) {
                case 'pending':
                    message = '거래 대기 중인 거래가 없습니다';
                    break;
                case 'ongoing':
                    message = '진행 중인 거래가 없습니다';
                    break;
                case 'completed':
                    message = '완료된 거래가 없습니다';
                    break;
                case 'cancelled':
                    message = '취소/만료된 거래가 없습니다';
                    break;
                default:
                    message = '거래가 없습니다';
            }

            container.innerHTML = `
                <div class="text-center py-12 bg-gray-50 rounded-lg">
                    <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
                    </svg>
                    <h3 class="mt-4 text-sm font-semibold text-gray-900">${message}</h3>
                    <p class="mt-2 text-sm text-gray-600">모든 거래가 여기에 표시됩니다.</p>
                </div>
            `;
            return;
        }

        container.innerHTML = deals.map(createDealCard).join('');
    }

    /** 탭 전환 */
    function switchTab(tabName) {
        currentTab = tabName;

        const tabButtons = document.querySelectorAll('.seller-deal-tab-btn');
        const tabContents = document.querySelectorAll('.seller-deal-tab-content');

        tabButtons.forEach((btn) => {
            const isActive = btn.dataset.tab === tabName;
            btn.classList.toggle('border-gray-900', isActive);
            btn.classList.toggle('text-gray-900', isActive);
            btn.classList.toggle('font-semibold', isActive);

            btn.classList.toggle('border-transparent', !isActive);
            btn.classList.toggle('text-gray-500', !isActive);
            btn.classList.toggle('font-medium', !isActive);
        });

        tabContents.forEach((content) => {
            content.classList.add('hidden');
        });

        const activeContent = document.getElementById(`tab-${tabName}`);
        if (activeContent) {
            activeContent.classList.remove('hidden');
        }

        renderTab(tabName);
    }

    /** 상세보기 버튼 위임 핸들러 */
    function bindDetailButtonHandler() {
        document.addEventListener('click', (event) => {
            const btn = event.target.closest('.seller-deal-detail-btn');
            if (!btn) return;

            const dealId = btn.dataset.dealId;
            if (!dealId) return;

            window.location.href = `/seller/deals/${dealId}`;
        });
    }

    /** 초기 탭 클릭 이벤트 바인딩 */
    function bindTabClickEvents() {
        const tabButtons = document.querySelectorAll('.seller-deal-tab-btn');
        tabButtons.forEach((btn) => {
            btn.addEventListener('click', () => {
                const tab = btn.dataset.tab;
                if (!tab || tab === currentTab) return;
                switchTab(tab);
            });
        });
    }

    /** 초기 데이터 로드 */
    async function init() {
        try {
            const data = await fetchJson(API_URL);
            allDeals = data.deals ?? [];

            updateTabCounts();
            bindTabClickEvents();
            bindDetailButtonHandler();
            switchTab('all');
        } catch (error) {
            console.error(error);
            alert(error.message || '판매자 거래 정보를 불러오는데 실패했습니다.');
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
