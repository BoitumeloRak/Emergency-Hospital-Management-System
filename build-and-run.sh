GREEN='\033[0;32m'
YELLOW='\033[1;3m'
NC='\033[03m' #no color

# function to display help
show_help() {
  echo "Options:"
  echo " build   Build all Docker images (./build-and-run.sh build)"
  echo " start   Start all services (./build-and-run.sh start)"
  echo " stop    Stop all services (./build-and-run.sh stop)"
  echo " restart Restart all services (./build-and-run.sh restart)"
  echo " log     Show logs for all services (./build-and-run.sh logs)"
  echo " clean   Remove all containers, networks, and volumes (./build-and-run.sh clean)"
  echo " help    Show this help message (./build-and-run.sh help)"
  echo -e "${NC}"
}

# function to build all services
build_services() {
  echo -e "${GREEN}Building all services...${NC}"
  mvn clean package
  docker-compose build
}

# function to start service
start_services() {
    echo -e "${GREEN}Starting all services...${NC}"
    docker-compose up -d
    echo -e "${YELLOW}ActiveMQ Console: http://localhost:8161 (admin/admin)${NC}"
    echo -e "${YELLOW}Web Application:  http://localhost:7070${NC}"
}

# function to stop service
stop_services() {
  echo -e "${YELLOW}Stopping all services...${NC}"
  docker-compose down
}

# function to show logs
show_logs() {
  echo -e "${GREEN} Showing logs (press Ctrl+C to exit)...${NC}"
  docker-compose logs -f
}

# function to clean up
clean_up() {
  echo -e "${YELLOW}Cleaning up...${NC}"
  docker-compose down -v
  docker system prune -f
  echo -e "${GREEN}Cleanup complete!${NC}"

}

# main script
case "$1" in
    build)
        build_services
        ;;
    start)
        start_services
        ;;
    stop)
        stop_services
        ;;
    restart)
        stop_services
        start_services
        ;;
    logs)
        show_logs
        ;;
    clean)
        clean_up
        ;;
    help|"")
        show_help
        ;;
    *)
        echo -e "${YELLOW}Unknown option: $1${NC}"
        show_help
        exit 1
        ;;
esac
exit 0

