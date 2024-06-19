package views.screen.home;

import common.exception.ViewCartException;
import controller.HomeController;
import controller.InvoiceListController;
import controller.ViewCartController;
import entity.cart.Cart;
import entity.media.Media;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import utils.Configs;
import utils.Utils;
import views.screen.BaseScreenHandler;
import views.screen.cart.CartScreenHandler;
import views.screen.invoicelist.InvoiceListHandler;
import views.screen.media.MediaDetailHandler;
import views.screen.popup.PopupScreen;
import views.screen.SessionManager;
import javafx.scene.Scene;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;


public class HomeScreenHandler extends BaseScreenHandler implements Initializable{

    public static Logger LOGGER = Utils.getLogger(HomeScreenHandler.class.getName());
    @FXML
    private Button signInButton;
    @FXML
    private Label numMediaInCart;

    @FXML
    private ImageView aimsImage;

    @FXML
    private Label currentPageLabel;

    @FXML
    private ImageView cartImage;

    @FXML
    private VBox vboxMedia1;

    @FXML
    private VBox vboxMedia2;

    @FXML
    private VBox vboxMedia3;

    @FXML
    private HBox hboxMedia;

    @FXML
    private TextField searchField;

    @FXML
    private SplitMenuButton splitMenuBtnSearch;


    @FXML
    private ImageView invoiceList;

    private List homeItems;

    private List displayedItems;

    public static HomeScreenHandler _instance;

    public HomeScreenHandler(Stage stage, String screenPath) throws IOException{
        super(stage, screenPath);
    }

    public Label getNumMediaCartLabel(){
        return this.numMediaInCart;
    }

    public HomeController getBController() {
        return (HomeController) super.getBController();
    }

    @Override
    public void show() {
        numMediaInCart.setText(Cart.getCart().getListMedia().size() + " media");
        if (SessionManager.isLoggedIn()) {
            signInButton.setText("Sign out");
        } else {
            signInButton.setText("Sign in");
        }
        super.show();
    }
    private int currentPage = 0;
    private final int itemsPerPage = 12;

    @FXML
    private void showNextMedia(MouseEvent event) {
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, displayedItems.size());
        if (endIndex < displayedItems.size()) {
            currentPage++;
            List<MediaHandler> displayedItems = updateMediaDisplay(this.displayedItems);
            addMediaHome(displayedItems);
        }
    }

    @FXML
    private void showPreviousMedia(MouseEvent event) {
        if (currentPage > 0) {
            currentPage--;
            List<MediaHandler> displayedItems = updateMediaDisplay(this.displayedItems);
            addMediaHome(displayedItems);
        }
    }

    private List<MediaHandler> updateMediaDisplay(List Items) {
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, Items.size());
        List<MediaHandler> displayedItems = new ArrayList<>(Items.subList(startIndex, endIndex));
        int totalPages = (int) Math.ceil((double) Items.size() / itemsPerPage);
        int currentDisplayPage = currentPage + 1;
        currentPageLabel.setText("Page " + currentDisplayPage + " of " + totalPages);
        return displayedItems;
    }
    @FXML
    public void handleSignInButtonClick(ActionEvent event) {
        // Lấy văn bản hiện tại của nút
        String currentText = signInButton.getText();

        // Thay đổi văn bản dựa trên trạng thái hiện tại
        if ("Sign in".equals(currentText)) {
            signInButton.setText("Sign out");
            Parent root1 = null;
            try {
                //Tải màn hình để đăng nhập
                root1 = FXMLLoader.load(getClass().getResource("/views/fxml/login_and_sign_up.fxml"));
                Scene scene1 = new Scene(root1);
                stage.setScene(scene1);
                stage.setTitle("Login/Register");
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            //ở đây tôi sẽ thực hện hành động đăng xuất
            signInButton.setText("Sign in");
            //Xóa trạng thái đăng nhập của người dùng
            SessionManager.setLoggedIn(false);
            HomeScreenHandler homeHandler = null;
            try {
                //Chuyển trả về màn hình ban đầu là mà hình có nút Sign in
                homeHandler = new HomeScreenHandler(stage, Configs.HOME_PATH);
                homeHandler.setScreenTitle("Home Screen Guest");
                homeHandler.setImage();
                homeHandler.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        setBController(new HomeController());
        try{
            List<Media> medium = getBController().getAllMedia();
            this.homeItems = new ArrayList<>();
            for (Media object : medium) {
                MediaHandler m1 = new MediaHandler(Configs.HOME_MEDIA_PATH, object, this);
                this.homeItems.add(m1);
            }
            this.displayedItems = this.homeItems;
        }catch (SQLException | IOException e){
            LOGGER.info("Initialize errors: " + e.getMessage());
        }

        // Home button
        aimsImage.setOnMouseClicked(e -> {
            List<MediaHandler> displayedItems = updateMediaDisplay(this.homeItems);
            addMediaHome(displayedItems);
        });

        // Cart button
        cartImage.setOnMouseClicked(e -> {
            CartScreenHandler cartScreen;
            try {
                LOGGER.info("User clicked to view cart");
                cartScreen = new CartScreenHandler(this.stage, Configs.CART_SCREEN_PATH);
                cartScreen.setHomeScreenHandler(this);
                cartScreen.setBController(new ViewCartController());
                cartScreen.requestToViewCart(this);
            } catch (IOException | SQLException e1) {
                throw new ViewCartException(Arrays.toString(e1.getStackTrace()).replaceAll(", ", "\n"));
            }
        });

        // Invoice button
        invoiceList.setOnMouseClicked(e -> {
            InvoiceListHandler invoiceListHandler;
            try {
                invoiceListHandler = new InvoiceListHandler(this.stage, Configs.INVOICE_LIST_PATH);
                invoiceListHandler.setHomeScreenHandler(this);
                invoiceListHandler.setBController(new InvoiceListController());
                invoiceListHandler.requestToInvoiceList(this);
            } catch (IOException | SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        addMediaHome(this.homeItems);
        addMenuItem(0, "Book", splitMenuBtnSearch);
        addMenuItem(1, "DVD", splitMenuBtnSearch);
        addMenuItem(2, "CD", splitMenuBtnSearch);
    }

    public void setImage(){
        // fix image path caused by fxml
        File file1 = new File(Configs.IMAGE_PATH + "/" + "Logo.png");
        Image img1 = new Image(file1.toURI().toString());
        aimsImage.setImage(img1);

        File file2 = new File(Configs.IMAGE_PATH + "/" + "cart.png");
        Image img2 = new Image(file2.toURI().toString());
        cartImage.setImage(img2);

        File file3 = new File(Configs.IMAGE_PATH + "/" + "invoice.png");
        Image img3 = new Image(file3.toURI().toString());
        invoiceList.setImage(img3);
    }

    public void addMediaHome(List items){
        ArrayList mediaItems = (ArrayList)((ArrayList) items).clone();
        hboxMedia.getChildren().forEach(node -> {
            VBox vBox = (VBox) node;
            vBox.getChildren().clear();
        });
        while(!mediaItems.isEmpty()){
            hboxMedia.getChildren().forEach(node -> {
                int vid = hboxMedia.getChildren().indexOf(node);
                VBox vBox = (VBox) node;
                while(vBox.getChildren().size()<3 && !mediaItems.isEmpty()){
                    MediaHandler media = (MediaHandler) mediaItems.get(0);
                    vBox.getChildren().add(media.getContent());
                    mediaItems.remove(media);
                }
            });
            return;
        }
    }

    public void addMenuItem(int position, String text, MenuButton menuButton){
        // Create a menu item
        MenuItem menuItem = new MenuItem();
        Label label = new Label();
        label.prefWidthProperty().bind(menuButton.widthProperty().subtract(31));
        label.setText(text);
        label.setTextAlignment(TextAlignment.RIGHT);
        menuItem.setGraphic(label);

        // Set action
        menuItem.setOnAction(e -> {
            // empty home media
            hboxMedia.getChildren().forEach(node -> {
                VBox vBox = (VBox) node;
                vBox.getChildren().clear();
            });

            // filter only media with the choosen category
            List<MediaHandler> filteredItems = new ArrayList<>();

            checkEmpty(filteredItems);
        });

        // Add to button
        menuButton.getItems().add(position, menuItem);
    }

    @FXML
    void searchButtonClicked(MouseEvent event) throws SQLException, IOException {
        String searchText = searchField.getText().toLowerCase().trim();
        List<Media> medium = getBController().getAllMedia();
        List<Media> filteredMedia = getBController().filterMediaByKeyWord(searchText,medium);
//        List<MediaHandler> filteredItems = filterMediaByKeyWord(searchText, homeItems);
        List<MediaHandler> filteredItems = convertMediaHandlerList(filteredMedia);
        checkEmpty(filteredItems);
    }

    private void checkEmpty(List<MediaHandler> filteredItems) {
        if (filteredItems.isEmpty()) {
            try {
                PopupScreen.error("No matching products.");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            currentPage = 0;
            this.displayedItems = filteredItems;
            List<MediaHandler> displayedItems = updateMediaDisplay(filteredItems);
            addMediaHome(displayedItems);
        }
    }

    public List<MediaHandler> filterMediaByKeyWord(String keyword, List<MediaHandler> items) {
        List<MediaHandler> filteredItems = new ArrayList<>();
        for (MediaHandler media : items) {
            if (media.getMedia().getTitle().toLowerCase().contains(keyword)) {
                filteredItems.add(media);
            }
        }
        return filteredItems;
    }

    public void handleClickDetail(Media media){
        MediaDetailHandler mediaDetailHandler;
        try {
            mediaDetailHandler = new MediaDetailHandler(this.stage, media,Configs.MEDIA_DETAIL_PATH);
            mediaDetailHandler.requestToDetail(this);
            mediaDetailHandler.setHomeScreenHandler(this);
        } catch (IOException | SQLException ex) {
            throw new RuntimeException(ex);
        }
    }


    public List<MediaHandler> convertMediaHandlerList(List<Media> items) throws SQLException, IOException {
        List<MediaHandler> mediaHandlerList = new ArrayList<>();
        for (Media media : items) {
            MediaHandler m1 = new MediaHandler(Configs.HOME_MEDIA_PATH, media, this);
            mediaHandlerList.add(m1);
        }
        return mediaHandlerList;
    }
}

